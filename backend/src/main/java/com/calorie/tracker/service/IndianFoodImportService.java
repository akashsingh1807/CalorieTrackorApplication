package com.calorie.tracker.service;

import com.calorie.tracker.config.IndianFoodImportProperties;
import com.calorie.tracker.model.IndianFood;
import com.calorie.tracker.repository.IndianFoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndianFoodImportService {

    private final IndianFoodImportProperties properties;
    private final IndianFoodRepository repository;

    public IndianFoodImportService(IndianFoodImportProperties properties, IndianFoodRepository repository) {
        this.properties = properties;
        this.repository = repository;
    }

    @Transactional
    public void importFromUrl() {
        if (!properties.isEnabled()) {
            System.out.println("Indian food import disabled.");
            return;
        }
        try {
            URL url = new URL(properties.getUrl());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String line;
                boolean first = true;
                List<IndianFood> batch = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    if (first) { // skip header
                        first = false;
                        continue;
                    }
                    // Expected CSV: foodId,name,calories,protein,fat,carbohydrate (quote‑escaped)
                    String[] parts = line.split(",");
                    if (parts.length < 6) continue;
                    // Remove surrounding quotes if present
                    String idStr = parts[0].replaceAll("\"", "").trim();
                    String name = parts[1].replaceAll("\"", "").trim();
                    String calStr = parts[2].replaceAll("\"", "").trim();
                    String protStr = parts[3].replaceAll("\"", "").trim();
                    String fatStr = parts[4].replaceAll("\"", "").trim();
                    String carbStr = parts[5].replaceAll("\"", "").trim();
                    IndianFood food = IndianFood.builder()
                            .foodId(idStr.isEmpty() ? null : Long.parseLong(idStr))
                            .name(name)
                            .calories(new BigDecimal(calStr))
                            .protein(new BigDecimal(protStr))
                            .fat(new BigDecimal(fatStr))
                            .carbohydrate(new BigDecimal(carbStr))
                            .build();
                    batch.add(food);
                    if (batch.size() >= 1000) {
                        repository.saveAll(batch);
                        batch.clear();
                    }
                }
                if (!batch.isEmpty()) {
                    repository.saveAll(batch);
                }
                System.out.println("Indian food import completed.");
            }
        } catch (Exception e) {
            System.err.println("Failed to import Indian food CSV: " + e.getMessage());
        }
    }
}
