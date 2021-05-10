package com.ag04.jhfcm.config;

import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Firebase Cloud Messaging related configurations.
 * 
 * @author dmadunic
 */
@Configuration
public class FcmConfig {
    
    @Value("${jhfcm.fcm.sa-json-file}")
    String fcmSaJsonFile;
    
    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
            .fromStream(new ClassPathResource(fcmSaJsonFile).getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions
            .builder()
            .setCredentials(googleCredentials)
            .build();

        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "jh-fcm");
        return FirebaseMessaging.getInstance(app);
    }
}
