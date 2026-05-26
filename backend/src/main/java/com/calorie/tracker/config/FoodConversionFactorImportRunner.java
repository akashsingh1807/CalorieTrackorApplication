package com.calorie.tracker.config;

import com.calorie.tracker.service.FoodConversionFactorImportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FoodConversionFactorImportRunner implements CommandLineRunner {

    private final FoodConversionFactorImportService importService;

    public FoodConversionFactorImportRunner(FoodConversionFactorImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(String... args) throws Exception {
        importService.importIfEnabled();
    }
}
