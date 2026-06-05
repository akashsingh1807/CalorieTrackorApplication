package com.calorie.tracker.service;

import com.calorie.tracker.config.FoodDataImportProperties;
import com.calorie.tracker.model.FoodConversionFactor;
import com.calorie.tracker.repository.FoodConversionFactorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class FoodDataImportRunner implements CommandLineRunner {

    private final FoodDataImportProperties properties;
    private final FoodConversionFactorRepository repository;

    public FoodDataImportRunner(FoodDataImportProperties properties, FoodConversionFactorRepository repository) {
        this.properties = properties;
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (!properties.isEnabled()) {
            return;
        }
        String path = properties.getFilePath();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean first = true;
            List<FoodConversionFactor> batch = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (first) { // skip header
                    first = false;
                    continue;
                }
                // Expected format: "id","protein","fat","carbohydrate"
                String[] parts = line.split(",");
                if (parts.length < 4) continue;
                Long id = Long.parseLong(parts[0].replaceAll("\"", "").trim());
                BigDecimal protein = new BigDecimal(parts[1].replaceAll("\"", "").trim());
                BigDecimal fat = new BigDecimal(parts[2].replaceAll("\"", "").trim());
                BigDecimal carb = new BigDecimal(parts[3].replaceAll("\"", "").trim());
                FoodConversionFactor entity = FoodConversionFactor.builder()
                        .id(id)
                        .protein(protein)
                        .fat(fat)
                        .carbohydrate(carb)
                        .build();
                batch.add(entity);
                // Batch save for performance
                if (batch.size() >= 1000) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }
        } catch (Exception e) {
            // Log error – using standard output for simplicity
            System.err.println("Failed to import FoodData Central CSV: " + e.getMessage());
        }
    }
}
