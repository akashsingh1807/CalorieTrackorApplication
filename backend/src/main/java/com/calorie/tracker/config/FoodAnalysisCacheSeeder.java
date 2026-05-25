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

    @Autowired
    private javax.sql.DataSource dataSource;

    private static final String[] PERSONAS = {"NONE", "STRICT_TRAINER", "INDIAN_MOM"};

    @PostConstruct
    public void seedCache() {
        try {
            logger.info("Initializing INDB & Kaggle Indian Food cache seeding...");

            if (foodAnalysisCacheRepository.count() == 0) {
                logger.info("Database cache is empty. Loading merged food inserts from resource file...");
                org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator = 
                    new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
                populator.addScript(new org.springframework.core.io.ClassPathResource("merged_food_inserts.sql"));
                populator.execute(dataSource);
                logger.info("Successfully loaded merged food inserts into database!");
            } else {
                logger.info("Database cache already contains records. Skipping merged food inserts loading.");
            }

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

            // 11. Chicken Biryani (g - scaled to 1g)
            seedItem("chicken biryani", "g", "[{\"name\":\"Chicken Biryani\",\"calories\":1.6,\"servingSize\":\"1g\",\"protein\":0.08,\"carbs\":0.19,\"fat\":0.053,\"fiber\":0.008,\"sugar\":0.01,\"sodium\":2.6,\"potassium\":1.8,\"calcium\":0.25,\"iron\":0.008,\"vitaminC\":0.01,\"vitaminD\":0.0}]");
            seedItem("biryani", "g", "[{\"name\":\"Chicken Biryani\",\"calories\":1.6,\"servingSize\":\"1g\",\"protein\":0.08,\"carbs\":0.19,\"fat\":0.053,\"fiber\":0.008,\"sugar\":0.01,\"sodium\":2.6,\"potassium\":1.8,\"calcium\":0.25,\"iron\":0.008,\"vitaminC\":0.01,\"vitaminD\":0.0}]");

            // 12. Veg Biryani (g - scaled to 1g)
            seedItem("veg biryani", "g", "[{\"name\":\"Veg Biryani\",\"calories\":1.3,\"servingSize\":\"1g\",\"protein\":0.03,\"carbs\":0.21,\"fat\":0.037,\"fiber\":0.012,\"sugar\":0.012,\"sodium\":2.2,\"potassium\":1.6,\"calcium\":0.3,\"iron\":0.006,\"vitaminC\":0.015,\"vitaminD\":0.0}]");

            // 13. Butter Chicken (g - scaled to 1g)
            seedItem("butter chicken", "g", "[{\"name\":\"Butter Chicken\",\"calories\":2.4,\"servingSize\":\"1g\",\"protein\":0.16,\"carbs\":0.08,\"fat\":0.16,\"fiber\":0.005,\"sugar\":0.03,\"sodium\":2.9,\"potassium\":2.2,\"calcium\":0.7,\"iron\":0.011,\"vitaminC\":0.01,\"vitaminD\":0.0}]");

            // 14. Samosa (piece)
            seedItem("samosa", "piece", "[{\"name\":\"Samosa\",\"calories\":260.0,\"servingSize\":\"1 piece\",\"protein\":3.5,\"carbs\":24.0,\"fat\":17.0,\"fiber\":2.1,\"sugar\":1.2,\"sodium\":320.0,\"potassium\":95.0,\"calcium\":18.0,\"iron\":1.2,\"vitaminC\":2.0,\"vitaminD\":0.0}]");

            // 15. Idli (piece)
            seedItem("idli", "piece", "[{\"name\":\"Idli\",\"calories\":58.0,\"servingSize\":\"1 piece\",\"protein\":1.6,\"carbs\":12.0,\"fat\":0.2,\"fiber\":0.8,\"sugar\":0.1,\"sodium\":115.0,\"potassium\":32.0,\"calcium\":8.0,\"iron\":0.3,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 16. Masala Dosa (piece)
            seedItem("masala dosa", "piece", "[{\"name\":\"Masala Dosa\",\"calories\":250.0,\"servingSize\":\"1 piece\",\"protein\":4.0,\"carbs\":45.0,\"fat\":8.0,\"fiber\":2.5,\"sugar\":0.5,\"sodium\":310.0,\"potassium\":120.0,\"calcium\":28.0,\"iron\":1.1,\"vitaminC\":0.5,\"vitaminD\":0.0}]");
            seedItem("dosa", "piece", "[{\"name\":\"Masala Dosa\",\"calories\":250.0,\"servingSize\":\"1 piece\",\"protein\":4.0,\"carbs\":45.0,\"fat\":8.0,\"fiber\":2.5,\"sugar\":0.5,\"sodium\":310.0,\"potassium\":120.0,\"calcium\":28.0,\"iron\":1.1,\"vitaminC\":0.5,\"vitaminD\":0.0}]");

            // 17. Medu Vada (piece)
            seedItem("medu vada", "piece", "[{\"name\":\"Medu Vada\",\"calories\":95.0,\"servingSize\":\"1 piece\",\"protein\":2.5,\"carbs\":11.0,\"fat\":4.5,\"fiber\":1.5,\"sugar\":0.2,\"sodium\":180.0,\"potassium\":75.0,\"calcium\":14.0,\"iron\":0.6,\"vitaminC\":0.0,\"vitaminD\":0.0}]");
            seedItem("vada", "piece", "[{\"name\":\"Medu Vada\",\"calories\":95.0,\"servingSize\":\"1 piece\",\"protein\":2.5,\"carbs\":11.0,\"fat\":4.5,\"fiber\":1.5,\"sugar\":0.2,\"sodium\":180.0,\"potassium\":75.0,\"calcium\":14.0,\"iron\":0.6,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 18. Poha (g - scaled to 1g)
            seedItem("poha", "g", "[{\"name\":\"Poha\",\"calories\":1.8,\"servingSize\":\"1g\",\"protein\":0.03,\"carbs\":0.35,\"fat\":0.032,\"fiber\":0.015,\"sugar\":0.015,\"sodium\":1.5,\"potassium\":0.9,\"calcium\":0.12,\"iron\":0.008,\"vitaminC\":0.03,\"vitaminD\":0.0}]");

            // 19. Dhokla (piece)
            seedItem("dhokla", "piece", "[{\"name\":\"Dhokla\",\"calories\":75.0,\"servingSize\":\"1 piece\",\"protein\":3.0,\"carbs\":12.0,\"fat\":2.0,\"fiber\":1.1,\"sugar\":1.5,\"sodium\":210.0,\"potassium\":55.0,\"calcium\":22.0,\"iron\":0.6,\"vitaminC\":0.8,\"vitaminD\":0.0}]");

            // 20. Gulab Jamun (piece)
            seedItem("gulab jamun", "piece", "[{\"name\":\"Gulab Jamun\",\"calories\":150.0,\"servingSize\":\"1 piece\",\"protein\":2.0,\"carbs\":28.0,\"fat\":4.0,\"fiber\":0.2,\"sugar\":24.0,\"sodium\":85.0,\"potassium\":40.0,\"calcium\":35.0,\"iron\":0.2,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 21. Bhatura (piece)
            seedItem("bhatura", "piece", "[{\"name\":\"Bhatura\",\"calories\":210.0,\"servingSize\":\"1 piece\",\"protein\":4.5,\"carbs\":32.0,\"fat\":7.0,\"fiber\":1.2,\"sugar\":0.5,\"sodium\":290.0,\"potassium\":65.0,\"calcium\":18.0,\"iron\":0.9,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 22. Palak Paneer (g - scaled to 1g)
            seedItem("palak paneer", "g", "[{\"name\":\"Palak Paneer\",\"calories\":1.4,\"servingSize\":\"1g\",\"protein\":0.075,\"carbs\":0.06,\"fat\":0.10,\"fiber\":0.015,\"sugar\":0.01,\"sodium\":2.8,\"potassium\":2.4,\"calcium\":2.1,\"iron\":0.02,\"vitaminC\":0.08,\"vitaminD\":0.0}]");

            // 23. Rajma Curry (g - scaled to 1g)
            seedItem("rajma", "g", "[{\"name\":\"Rajma Curry\",\"calories\":1.2,\"servingSize\":\"1g\",\"protein\":0.048,\"carbs\":0.18,\"fat\":0.032,\"fiber\":0.045,\"sugar\":0.015,\"sodium\":2.4,\"potassium\":2.9,\"calcium\":0.35,\"iron\":0.015,\"vitaminC\":0.02,\"vitaminD\":0.0}]");
            seedItem("rajma curry", "g", "[{\"name\":\"Rajma Curry\",\"calories\":1.2,\"servingSize\":\"1g\",\"protein\":0.048,\"carbs\":0.18,\"fat\":0.032,\"fiber\":0.045,\"sugar\":0.015,\"sodium\":2.4,\"potassium\":2.9,\"calcium\":0.35,\"iron\":0.015,\"vitaminC\":0.02,\"vitaminD\":0.0}]");

            // 24. Naan (piece)
            seedItem("naan", "piece", "[{\"name\":\"Naan\",\"calories\":260.0,\"servingSize\":\"1 piece\",\"protein\":8.0,\"carbs\":45.0,\"fat\":5.0,\"fiber\":2.2,\"sugar\":1.8,\"sodium\":380.0,\"potassium\":90.0,\"calcium\":45.0,\"iron\":1.6,\"vitaminC\":0.0,\"vitaminD\":0.0}]");

            // 25. Tandoori Chicken (g - scaled to 1g)
            seedItem("tandoori chicken", "g", "[{\"name\":\"Tandoori Chicken\",\"calories\":1.5,\"servingSize\":\"1g\",\"protein\":0.20,\"carbs\":0.01,\"fat\":0.07,\"fiber\":0.0,\"sugar\":0.0,\"sodium\":2.8,\"potassium\":2.6,\"calcium\":0.12,\"iron\":0.009,\"vitaminC\":0.005,\"vitaminD\":0.0}]");

            logger.info("INDB & Kaggle Indian Food cache seeding completed successfully!");
        } catch (Exception e) {
            logger.error("Failed to seed cache values", e);
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
