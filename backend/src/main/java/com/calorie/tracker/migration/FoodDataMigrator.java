package com.calorie.tracker.migration;

import com.calorie.tracker.dto.FoodItemDto;
import com.calorie.tracker.repository.FirestoreFoodRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Reads the CSV file defined in {@code application.yml} and imports each row as a Firestore document.
 * The CSV is expected to have the columns: name, calories, protein, carbs, fat, fiber, sugar, sodium,
 * potassium, calcium, iron, vitaminC, vitaminD. Adjust as needed.
 */
@Component
public class FoodDataMigrator implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(FoodDataMigrator.class);

    private final FirestoreFoodRepository repository;

    public FoodDataMigrator(FirestoreFoodRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Path is configured in application.yml under gemini.fooddata.import.filePath
        String csvPath = System.getProperty("fooddata.csv.path",
                "/Users/akashsingh/Downloads/FoodData_Central_foundation_food_csv_2026-04-30/food_calorie_conversion_factor.csv");
        logger.info("Starting migration of food data from CSV: {}", csvPath);
        try (CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            String[] header = reader.readNext(); // assume first line header
            if (header == null) {
                logger.warn("CSV file is empty, aborting migration.");
                return;
            }
            int count = 0;
            String[] line;
            while ((line = reader.readNext()) != null) {
                // Basic safety check for column count
                if (line.length < 13) {
                    logger.warn("Skipping malformed line {}: {}", count + 2, (Object) line);
                    continue;
                }
                FoodItemDto dto = mapLineToDto(line);
                try {
                    repository.save(dto);
                    count++;
                } catch (ExecutionException | InterruptedException e) {
                    logger.error("Failed to save food item {}: {}", dto.getName(), e.getMessage(), e);
                }
            }
            logger.info("Migration completed. Imported {} records into Firestore.", count);
        } catch (IOException | CsvValidationException e) {
            logger.error("Error reading CSV file: {}", e.getMessage(), e);
        }
    }

    private FoodItemDto mapLineToDto(String[] cols) {
        // Assuming column order matches the DTO fields; adjust indexes if needed.
        FoodItemDto dto = new FoodItemDto();
        dto.setName(cols[0]);
        dto.setServingSize("100g");
        dto.setCalories(parseDouble(cols[1]));
        dto.setProtein(parseDouble(cols[2]));
        dto.setCarbs(parseDouble(cols[3]));
        dto.setFat(parseDouble(cols[4]));
        dto.setFiber(parseDouble(cols[5]));
        dto.setSugar(parseDouble(cols[6]));
        dto.setSodium(parseDouble(cols[7]));
        dto.setPotassium(parseDouble(cols[8]));
        dto.setCalcium(parseDouble(cols[9]));
        dto.setIron(parseDouble(cols[10]));
        dto.setVitaminC(parseDouble(cols[11]));
        dto.setVitaminD(parseDouble(cols[12]));
        return dto;
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
