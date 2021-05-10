package com.ag04.jhfcm.producer;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import kong.unirest.JsonNode;
import kong.unirest.Unirest;


/**
 * Producer to send messages wiht Chuck Norris jokes to target fcm messages. 
 * 
 * @author dmadunic
 */
public class SendJokesJob {
    private static final Logger log = LoggerFactory.getLogger(SendJokesJob.class);

    @Value("${jhfcm.fcm.topic.jokes}")
    private String jokeTopic;

    private long count = 0;
    
    private final FirebaseMessaging firebaseMessaging;
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");

    public SendJokesJob(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }
 
    @Scheduled(initialDelay = 60000, fixedDelay = 30000)
    public void sendChuckQuotes() {
        JsonNode jokeResponse = Unirest.get("http://api.icndb.com/jokes/random").asJson().getBody();
        String id = jokeResponse.getObject().getJSONObject("value").getString("id");
        String joke = jokeResponse.getObject().getJSONObject("value").getString("joke");
        count++;
        Map<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("seq", String.valueOf(this.count));
        data.put("time", ZonedDateTime.now().format(formatter));
        data.put("joke", joke);
        data.put("ts", String.valueOf(Instant.now()));
        try {
            log.debug("--> Sending FCM message to topic='{}' wiht data={}", jokeTopic, data);
            String messageId = sendDataMessage(data, jokeTopic);
            log.info("--> FCM message sent wiht id={}", messageId);
        } catch (FirebaseMessagingException e) {
            log.error("FAILED to send chuck joke message:", e);
        }
    }

    private String sendDataMessage(Map<String, String> data, String topic) throws FirebaseMessagingException {  
        Message message = Message
            .builder()
            .setTopic(topic)
            .putAllData(data)
            .setWebpushConfig(WebpushConfig
                .builder()
                .putHeader("ttl", "300")
                .build()
        ).build();
        String messageId = firebaseMessaging.send(message);
        return messageId;
    }
}
