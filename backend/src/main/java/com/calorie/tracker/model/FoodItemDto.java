package com.calorie.tracker.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Simple Data Transfer Object used by {@link com.calorie.tracker.service.FoodInfoService}.
 * Contains the macro‑nutrient fields required by the API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodItemDto {
    private String name;
    private String servingSize;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private double fiber;
    private double sugar;
    private double sodium;
    private double potassium;
    private double calcium;
    private double iron;
    private double vitaminC;
    private double vitaminD;
}
