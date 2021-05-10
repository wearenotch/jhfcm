package com.ag04.jhfcm.service;

import java.util.List;
import java.util.Map;

import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

@Service
public class FirebaseMessagingService implements MessagingService {

    private final FirebaseMessaging firebaseMessaging;

    public FirebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    public String sendDataMessage(Map<String, String> data, String topic) throws FirebaseMessagingException {
        
        Message message = Message
            .builder()
            .setTopic(topic)
            .putAllData(data)
            .setWebpushConfig(WebpushConfig
                .builder()
                .putHeader("ttl", "300")
                .build()
        ).build();
        /*
        .setNotification(new WebpushNotification(
                "Background Title (server)",
                "Background Body (server)",
                "mail2.png"
            )
        */
        return firebaseMessaging.send(message);

    }

    public String sendMessage(Message message) throws FirebaseMessagingException {
        return firebaseMessaging.send(message);
    }

    public BatchResponse sendBatchOfMessages(List<Message> messages) throws FirebaseMessagingException {
        return firebaseMessaging.sendAll(messages);
    }

}
