package com.calorie.tracker.model

import kotlinx.serialization.Serializable

@Serializable
data class FoodItemDto(
    val name: String = "",
    val servingSize: String = "1 serving",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    // Micronutrients (all optional — default 0.0 if backend omits them)
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val potassium: Double = 0.0,
    val calcium: Double = 0.0,
    val iron: Double = 0.0,
    val vitaminC: Double = 0.0,
    val vitaminD: Double = 0.0
)

@Serializable
data class AnalyzeTextRequest(
    val text: String
)

@Serializable
data class AnalyzeTextResponse(
    val foodItems: List<FoodItemDto> = emptyList(),
    val totalCalories: Double = 0.0
)
