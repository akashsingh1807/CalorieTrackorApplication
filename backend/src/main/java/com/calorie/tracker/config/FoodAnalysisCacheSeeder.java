package com.calorie.tracker.config;

import com.calorie.tracker.model.FoodAnalysisCache;
import com.calorie.tracker.repository.FoodAnalysisCacheRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FoodAnalysisCacheSeeder {

    private static final Logger logger = LoggerFactory.getLogger(FoodAnalysisCacheSeeder.class);

    @Autowired
    private FoodAnalysisCacheRepository foodAnalysisCacheRepository;

    private static final String[] PERSONAS = {"NONE", "STRICT_TRAINER", "INDIAN_MOM"};

    @PostConstruct
    public void seedCache() {
        try {
            logger.info("Initializing INDB (Indian Nutrient Databank) cache seeding...");

            // 1. Roti / Chapati (piece)
            seedItem("roti", "piece", "[{\"name\":\"Roti\",\"calories\":75.0,\"servingSize\":\"1 piece\",\"protein\":2.6,\"carbs\":15.0,\"fat\":0.4,\"fiber\":2.2,\"sugar\":0.1,\"sodium\":2.0,\"potassium\":45.0,\"calcium\":10.0,\"iron\":0.8,\"vitaminC\":0.0,\"vitaminD\":0.0}]");
            seedItem("chapati", "piece", "[{\"name\":\"Chapati\",\"calories\":75.0,\"servingSize\":\"1 piece\",\"protein\":2.6,\"carbs\":15.0,\"fat\":0.4,\"fiber\":2.2,\"sugar\":0.1,\"sodium\":2.0,\"potassium\":45.0,\"calcium\":10.0,\"iron\":0.8,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 2. Boiled Egg (piece)
            seedItem("egg", "piece", "[{\"name\":\"Boiled Egg\",\"calories\":78.0,\"servingSize\":\"1 piece\",\"protein\":6.3,\"carbs\":0.6,\"fat\":5.3,\"fiber\":0.0,\"sugar\":0.6,\"sodium\":62.0,\"potassium\":63.0,\"calcium\":25.0,\"iron\":0.6,\"vitaminC\":0.0,\"vitaminD\":1.1}]");
            seedItem("boiled egg", "piece", "[{\"name\":\"Boiled Egg\",\"calories\":78.0,\"servingSize\":\"1 piece\",\"protein\":6.3,\"carbs\":0.6,\"fat\":5.3,\"fiber\":0.0,\"sugar\":0.6,\"sodium\":62.0,\"potassium\":63.0,\"calcium\":25.0,\"iron\":0.6,\"vitaminC\":0.0,\"vitaminD\":1.1}]");

            // 3. Cooked Rice (g - scaled to 1g)
            seedItem("cooked rice", "g", "[{\"name\":\"Cooked Rice\",\"calories\":1.3,\"servingSize\":\"1g\",\"protein\":0.027,\"carbs\":0.28,\"fat\":0.003,\"fiber\":0.004,\"sugar\":0.0,\"sodium\":0.01,\"potassium\":0.35,\"calcium\":0.1,\"iron\":0.002,\"vitaminC\":0.0,\"vitaminD\":0.0}]");
            seedItem("rice", "g", "[{\"name\":\"Cooked Rice\",\"calories\":1.3,\"servingSize\":\"1g\",\"protein\":0.027,\"carbs\":0.28,\"fat\":0.003,\"fiber\":0.004,\"sugar\":0.0,\"sodium\":0.01,\"potassium\":0.35,\"calcium\":0.1,\"iron\":0.002,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 4. Cooked Dal (g - scaled to 1g)
            seedItem("cooked dal", "g", "[{\"name\":\"Cooked Dal\",\"calories\":1.15,\"servingSize\":\"1g\",\"protein\":0.07,\"carbs\":0.20,\"fat\":0.005,\"fiber\":0.03,\"sugar\":0.01,\"sodium\":0.15,\"potassium\":2.1,\"calcium\":0.2,\"iron\":0.018,\"vitaminC\":0.01,\"vitaminD\":0.0}]");
            seedItem("dal", "g", "[{\"name\":\"Cooked Dal\",\"calories\":1.15,\"servingSize\":\"1g\",\"protein\":0.07,\"carbs\":0.20,\"fat\":0.005,\"fiber\":0.03,\"sugar\":0.01,\"sodium\":0.15,\"potassium\":2.1,\"calcium\":0.2,\"iron\":0.018,\"vitaminC\":0.01,\"vitaminD\":0.0}]");

            // 5. Paneer Butter Masala (g - scaled to 1g)
            seedItem("paneer butter masala", "g", "[{\"name\":\"Paneer Butter Masala\",\"calories\":2.3,\"servingSize\":\"1g\",\"protein\":0.09,\"carbs\":0.06,\"fat\":0.19,\"fiber\":0.008,\"sugar\":0.02,\"sodium\":1.8,\"potassium\":1.5,\"calcium\":1.8,\"iron\":0.01,\"vitaminC\":0.02,\"vitaminD\":0.0}]");

            // 6. Chicken Tikka Masala (g - scaled to 1g)
            seedItem("chicken tikka masala", "g", "[{\"name\":\"Chicken Tikka Masala\",\"calories\":1.95,\"servingSize\":\"1g\",\"protein\":0.14,\"carbs\":0.05,\"fat\":0.12,\"fiber\":0.005,\"sugar\":0.015,\"sodium\":2.2,\"potassium\":2.0,\"calcium\":0.8,\"iron\":0.012,\"vitaminC\":0.015,\"vitaminD\":0.0}]");

            // 7. Apple (piece)
            seedItem("apple", "piece", "[{\"name\":\"Apple\",\"calories\":95.0,\"servingSize\":\"1 piece\",\"protein\":0.5,\"carbs\":25.0,\"fat\":0.3,\"fiber\":4.4,\"sugar\":19.0,\"sodium\":2.0,\"potassium\":195.0,\"calcium\":11.0,\"iron\":0.2,\"vitaminC\":8.4,\"vitaminD\":0.0}]");

            // 8. Milk (ml - scaled to 1ml)
            seedItem("milk", "ml", "[{\"name\":\"Milk\",\"calories\":0.6,\"servingSize\":\"1ml\",\"protein\":0.032,\"carbs\":0.048,\"fat\":0.0325,\"fiber\":0.0,\"sugar\":0.05,\"sodium\":0.43,\"potassium\":1.43,\"calcium\":1.13,\"iron\":0.0,\"vitaminC\":0.0,\"vitaminD\":0.01}]");

            // 9. Tea / Chai (cup)
            seedItem("tea", "cup", "[{\"name\":\"Masala Chai\",\"calories\":80.0,\"servingSize\":\"1 cup\",\"protein\":1.5,\"carbs\":12.0,\"fat\":2.5,\"fiber\":0.0,\"sugar\":10.0,\"sodium\":35.0,\"potassium\":60.0,\"calcium\":45.0,\"iron\":0.1,\"vitaminC\":0.0,\"vitaminD\":0.0}]");
            seedItem("chai", "cup", "[{\"name\":\"Masala Chai\",\"calories\":80.0,\"servingSize\":\"1 cup\",\"protein\":1.5,\"carbs\":12.0,\"fat\":2.5,\"fiber\":0.0,\"sugar\":10.0,\"sodium\":35.0,\"potassium\":60.0,\"calcium\":45.0,\"iron\":0.1,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 10. Aloo Paratha (piece)
            seedItem("aloo paratha", "piece", "[{\"name\":\"Aloo Paratha\",\"calories\":290.0,\"servingSize\":\"1 piece\",\"protein\":5.0,\"carbs\":42.0,\"fat\":11.0,\"fiber\":4.5,\"sugar\":2.0,\"sodium\":250.0,\"potassium\":180.0,\"calcium\":22.0,\"iron\":1.8,\"vitaminC\":3.0,\"vitaminD\":0.0}]");

            logger.info("INDB cache seeding completed successfully!");
        } catch (Exception e) {
            logger.error("Failed to seed INDB values to database cache", e);
        }
    }

    private void seedItem(String baseFood, String unit, String jsonResponse) {
        for (String persona : PERSONAS) {
            String queryKey = persona + ":scaled:" + baseFood + ":" + unit;
            try {
                if (foodAnalysisCacheRepository.findByQueryKey(queryKey).isEmpty()) {
                    FoodAnalysisCache entry = FoodAnalysisCache.builder()
                            .queryKey(queryKey)
                            .responseJson(jsonResponse)
                            .createdAt(LocalDateTime.now())
                            .build();
                    foodAnalysisCacheRepository.save(entry);
                    logger.debug("Seeded cache entry for key: {}", queryKey);
                }
            } catch (Exception e) {
                logger.error("Error saving cache seed for key {}: {}", queryKey, e.getMessage());
            }
        }
    }
}
