package com.calorie.tracker.dto;

import lombok.Data;

@Data
public class GeminiFoodResponseDto {
    private String reasoning;
    private Boolean isFood;
    private String foodName;
    private Double calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;
    private Double servingSize;
    private String servingUnit;
}
