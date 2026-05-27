package com.calorie.tracker.repository;

import com.calorie.tracker.model.FoodItemDto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreFoodRepository {
    private final Firestore firestore;
    private final CollectionReference collection;

    public FirestoreFoodRepository(Firestore firestore) {
        this.firestore = firestore;
        this.collection = firestore.collection("foods");
    }

    public void save(FoodItemDto dto) throws ExecutionException, InterruptedException {
        // Use a sanitized name as the document ID
        String docId = dto.getName().toLowerCase().replaceAll("\\s+", "_");
        collection.document(docId).set(dto).get();
    }

    /**
     * Retrieve a FoodItemDto by name (case‑insensitive). Returns Optional.empty() if not found.
     */
    public java.util.Optional<com.calorie.tracker.model.FoodItemDto> findByName(String name) throws ExecutionException, InterruptedException {
        String docId = name.toLowerCase().replaceAll("\\s+", "_");
        com.google.cloud.firestore.DocumentSnapshot snapshot = collection.document(docId).get().get();
        if (snapshot.exists()) {
            com.calorie.tracker.model.FoodItemDto dto = snapshot.toObject(com.calorie.tracker.model.FoodItemDto.class);
            return java.util.Optional.ofNullable(dto);
        }
        return java.util.Optional.empty();
    }
        
}
