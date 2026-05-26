package com.calorie.tracker.service;

import com.calorie.tracker.model.FoodConversionFactor;
import com.calorie.tracker.repository.FoodConversionFactorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class FoodConversionFactorImportService {

    private final FoodConversionFactorRepository repository;
    private final boolean enabled;
    private final String filePath;

    public FoodConversionFactorImportService(FoodConversionFactorRepository repository,
                                            @Value("${gemini.fooddata.import.enabled:false}") boolean enabled,
                                            @Value("${gemini.fooddata.import.filePath:}") String filePath) {
        this.repository = repository;
        this.enabled = enabled;
        this.filePath = filePath;
    }

    public void importIfEnabled() throws IOException {
        if (!enabled) {
            return;
        }
        File csvFile = new File(filePath);
        if (!csvFile.exists()) {
            throw new FileNotFoundException("FoodData CSV not found at " + filePath);
        }
        try (BufferedReader br = Files.newBufferedReader(csvFile.toPath())) {
            // Assuming the CSV header includes columns: fdcId,name,protein,fat,carbohydrate
            String header = br.readLine(); // skip header line
            String line;
            List<FoodConversionFactor> batch = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",");
                if (cols.length < 5) {
                    continue; // skip malformed rows
                }
                FoodConversionFactor entity = new FoodConversionFactor();
                entity.setFdcId(cols[0].trim());
                entity.setName(cols[1].trim());
                entity.setProtein(parseDecimal(cols[2]));
                entity.setFat(parseDecimal(cols[3]));
                entity.setCarbohydrate(parseDecimal(cols[4]));
                // Calculate calories if macro values present
                if (entity.getProtein() != null && entity.getFat() != null && entity.getCarbohydrate() != null) {
                    BigDecimal calories = entity.getProtein().multiply(BigDecimal.valueOf(4))
                            .add(entity.getFat().multiply(BigDecimal.valueOf(9)))
                            .add(entity.getCarbohydrate().multiply(BigDecimal.valueOf(4)));
                    entity.setCalories(calories);
                }
                batch.add(entity);
                if (batch.size() >= 500) {
                    repository.saveAll(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                repository.saveAll(batch);
            }
        }
    }

    private BigDecimal parseDecimal(String s) {
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
