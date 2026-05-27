package com.calorie.tracker.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirestoreConfig {

    /**
     * Initialise the Firebase SDK using a service‑account JSON file.
     * The path to the file should be supplied via the property
     * {@code firebase.service.account.path} in {@code application.yml}.
     */
    @Bean
    public Firestore firestore() throws IOException {
        // The service‑account key location is defined in application.yml
        String serviceAccountPath = System.getProperty("firebase.service.account.path",
                "src/main/resources/firebase-service-account.json");
        FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        // Initialise only once – repeated calls will return the existing instance.
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
        return FirestoreClient.getFirestore();
    }
}
