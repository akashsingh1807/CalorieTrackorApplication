package com.calorie.tracker.repository;

import com.calorie.tracker.dto.FoodItemDto;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class FirestoreFoodRepository {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreFoodRepository.class);

    private final Firestore firestore;
    private final CollectionReference collection;

    public FirestoreFoodRepository(Firestore firestore) {
        this.firestore = firestore;
        this.collection = firestore.collection("foods");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    /** Converts a display name into the Firestore document ID convention (snake_case). */
    private String toDocId(String name) {
        return name.toLowerCase()
                   .trim()
                   .replaceAll("[^a-z0-9\\s]", "")   // strip non-alphanumeric except spaces
                   .replaceAll("\\s+", "_");
    }

    // ─── Writes ──────────────────────────────────────────────────────────────────

    /**
     * Persists a single food item. Uses the sanitised name as document ID.
     * A {@code source} field ("llm") is added so you can track LLM-generated records.
     */
    public void save(FoodItemDto dto) throws ExecutionException, InterruptedException {
        save(dto, "manual");
    }

    /**
     * Persists a single food item with an explicit source tag.
     *
     * @param source "llm", "manual", "import", etc.
     */
    public void save(FoodItemDto dto, String source) throws ExecutionException, InterruptedException {
        if (dto == null || dto.getName() == null || dto.getName().isBlank()) return;

        String docId = toDocId(dto.getName());
        Map<String, Object> data = buildDataMap(dto, source);
        collection.document(docId).set(data).get();
        logger.info("Firestore: saved food '{}' (source={})", dto.getName(), source);
    }

    /**
     * Saves a food item that was returned by the LLM so we can reuse it later.
     */
    public void saveFromLlm(FoodItemDto dto) {
        try {
            save(dto, "llm");
        } catch (Exception e) {
            logger.warn("Firestore: failed to persist LLM result for '{}': {}", dto.getName(), e.getMessage());
        }
    }

    // ─── Reads ───────────────────────────────────────────────────────────────────

    /**
     * Exact look-up by document ID (derived from the food name).
     */
    public Optional<FoodItemDto> findByName(String name) throws ExecutionException, InterruptedException {
        String docId = toDocId(name);
        DocumentSnapshot snapshot = collection.document(docId).get().get();
        if (snapshot.exists()) {
            FoodItemDto dto = snapshotToDto(snapshot);
            return Optional.ofNullable(dto);
        }
        return Optional.empty();
    }

    /**
     * Try multiple candidate names in order and return the first hit.
     * Useful for compound queries like "daal chawal" where we try:
     *   ["daal chawal", "dal chawal", "daal", "dal"]
     */
    public Optional<FoodItemDto> findByAnyName(List<String> candidates) {
        for (String candidate : candidates) {
            try {
                Optional<FoodItemDto> result = findByName(candidate);
                if (result.isPresent()) {
                    logger.info("Firestore: hit for candidate '{}'", candidate);
                    return result;
                }
            } catch (Exception e) {
                logger.warn("Firestore lookup failed for '{}': {}", candidate, e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * Find all documents whose name field contains the given substring.
     * Firestore does not support LIKE queries, so we stream the collection
     * and filter in-memory (fine for a few thousand items).
     */
    public List<FoodItemDto> searchByNameContaining(String keyword) {
        List<FoodItemDto> results = new ArrayList<>();
        try {
            String lowerKeyword = keyword.toLowerCase().trim();
            QuerySnapshot snapshot = collection.get().get();
            for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
                String docName = doc.getString("name");
                if (docName != null && docName.toLowerCase().contains(lowerKeyword)) {
                    FoodItemDto dto = snapshotToDto(doc);
                    if (dto != null) results.add(dto);
                }
            }
        } catch (Exception e) {
            logger.error("Firestore searchByNameContaining('{}') failed: {}", keyword, e.getMessage());
        }
        return results;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    private Map<String, Object> buildDataMap(FoodItemDto dto, String source) {
        return Map.ofEntries(
            Map.entry("name",        dto.getName()        != null ? dto.getName()        : ""),
            Map.entry("servingSize", dto.getServingSize() != null ? dto.getServingSize() : ""),
            Map.entry("calories",    orZero(dto.getCalories())),
            Map.entry("protein",     orZero(dto.getProtein())),
            Map.entry("carbs",       orZero(dto.getCarbs())),
            Map.entry("fat",         orZero(dto.getFat())),
            Map.entry("fiber",       orZero(dto.getFiber())),
            Map.entry("sugar",       orZero(dto.getSugar())),
            Map.entry("sodium",      orZero(dto.getSodium())),
            Map.entry("potassium",   orZero(dto.getPotassium())),
            Map.entry("calcium",     orZero(dto.getCalcium())),
            Map.entry("iron",        orZero(dto.getIron())),
            Map.entry("vitaminC",    orZero(dto.getVitaminC())),
            Map.entry("vitaminD",    orZero(dto.getVitaminD())),
            Map.entry("source",      source)
        );
    }

    private FoodItemDto snapshotToDto(DocumentSnapshot snapshot) {
        if (!snapshot.exists()) return null;
        FoodItemDto dto = new FoodItemDto();
        dto.setName(snapshot.getString("name"));
        dto.setServingSize(snapshot.getString("servingSize"));
        dto.setCalories(getDouble(snapshot, "calories"));
        dto.setProtein(getDouble(snapshot, "protein"));
        dto.setCarbs(getDouble(snapshot, "carbs"));
        dto.setFat(getDouble(snapshot, "fat"));
        dto.setFiber(getDouble(snapshot, "fiber"));
        dto.setSugar(getDouble(snapshot, "sugar"));
        dto.setSodium(getDouble(snapshot, "sodium"));
        dto.setPotassium(getDouble(snapshot, "potassium"));
        dto.setCalcium(getDouble(snapshot, "calcium"));
        dto.setIron(getDouble(snapshot, "iron"));
        dto.setVitaminC(getDouble(snapshot, "vitaminC"));
        dto.setVitaminD(getDouble(snapshot, "vitaminD"));
        return dto;
    }

    private double getDouble(DocumentSnapshot snapshot, String field) {
        Double val = snapshot.getDouble(field);
        return val != null ? val : 0.0;
    }

    private double orZero(Double val) {
        return val != null ? val : 0.0;
    }
}
