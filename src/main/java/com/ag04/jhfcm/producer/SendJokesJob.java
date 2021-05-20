package com.ag04.jhfcm.producer;
import java.time.Instant;
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
import org.springframework.stereotype.Component;

import kong.unirest.JsonNode;
import kong.unirest.Unirest;


/**
 * Producer to send messages wiht Chuck Norris jokes to target fcm messages. 
 * 
 * @author dmadunic
 */
@Component
public class SendJokesJob {
    private static final Logger log = LoggerFactory.getLogger(SendJokesJob.class);
    private static final String MSG_TTL = "300"; // seconds

    @Value("${jhfcm.fcm.topic.jokes}")
    private String jokeTopic;

    private long count = 0;
    
    private final FirebaseMessaging firebaseMessaging;
    
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
        data.put("joke", joke);
        data.put("ts", String.valueOf(Instant.now())); // message timestamp
        try {
            log.debug("--> Sending FCM message (id={}) to topic='{}'", id, jokeTopic);
            String messageId = sendDataMessage(data, jokeTopic);
            log.info("--> FCM message sent to topic='{}' wiht messageId={}", jokeTopic, messageId);
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
                .putHeader("ttl", MSG_TTL)
                .build()
        ).build();
        String messageId = firebaseMessaging.send(message);
        return messageId;
    }
}
