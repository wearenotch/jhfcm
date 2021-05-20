package com.ag04.jhfcm.web.rest;

import java.util.Collections;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for FCM topic subscription management (subscribe/unsubscribe).
 * 
 * @author dmadunic
 */
@RestController
@RequestMapping("/api")
public class FcmRegistrationController {
    private final Logger log = LoggerFactory.getLogger(FcmRegistrationController.class);

    @Value("${jhfcm.fcm.topic.jokes}")
    private String jokeTopic;

    private final FirebaseMessaging firebaseMessaging;

    public FcmRegistrationController(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    @PostMapping("/fcm/registration")
    public ResponseEntity<String> subscribe(@RequestBody String token) throws FirebaseMessagingException {
        log.debug("Processing Topic subscription request for token='{}'", token);
        TopicManagementResponse response = firebaseMessaging.subscribeToTopic(Collections.singletonList(token), jokeTopic);
        if (response.getSuccessCount() > 0) {
            log.info("--> FINISHED processing subscription to topic: '{}' for token '{}'", jokeTopic, token);
            return ResponseEntity.ok("");
        } else {
            String reason = getErrorReason(response);
            log.error("--> FAILED processing subscription to topic: '{}'! Reason: {} / for token={}", new Object[] { jokeTopic, reason, token });
            return ResponseEntity.ok(reason);
        }
    }

    @PutMapping("/fcm/registration")
    public ResponseEntity<String> unsubscribe(@RequestBody String token) throws FirebaseMessagingException {
        log.debug("Processing Topic unsubscription request for token='{}'", token);
        TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(Collections.singletonList(token), jokeTopic);
        if (response.getSuccessCount() > 0) {
            log.info("--> FINISHED processing unsubscription from topic: '{}' for token '{}'", jokeTopic, token);
            return ResponseEntity.ok("");
        } else {
            String reason = getErrorReason(response);
            log.error("--> FAILED processing unsubscription from topic: '{}'! Reason: {} / for token={}", new Object[] { jokeTopic, reason, token });
            return ResponseEntity.ok(reason);
        }
    }
    
    private String getErrorReason(TopicManagementResponse response) {
        String reason = response.getErrors().get(0).getReason();
        return reason;
    }
}
