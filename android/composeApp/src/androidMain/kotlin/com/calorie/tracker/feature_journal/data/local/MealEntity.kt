package com.calorie.tracker.feature_journal.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.calorie.tracker.model.Meal

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String,
    val imageUrl: String?,
    val timestamp: Long,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val isSynced: Boolean = false,
    // Micronutrients (nullable for backward compat with existing DB rows)
    val totalFiber: Double = 0.0,
    val totalSugar: Double = 0.0,
    val totalSodium: Double = 0.0,
    val totalPotassium: Double = 0.0,
    val totalCalcium: Double = 0.0,
    val totalIron: Double = 0.0,
    val totalVitaminC: Double = 0.0,
    val totalVitaminD: Double = 0.0,
    val rawTextInput: String? = null,
    val isAiLogged: Boolean = false,
    val mealCategory: String = "BREAKFAST",
    val itemsData: String = ""
) {
    fun toDomain(): Meal = Meal(
        id = id,
        mealType = mealType,
        imageUrl = imageUrl,
        timestamp = timestamp,
        totalCalories = totalCalories,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        isSynced = isSynced,
        totalFiber = totalFiber,
        totalSugar = totalSugar,
        totalSodium = totalSodium,
        totalPotassium = totalPotassium,
        totalCalcium = totalCalcium,
        totalIron = totalIron,
        totalVitaminC = totalVitaminC,
        totalVitaminD = totalVitaminD,
        rawTextInput = rawTextInput,
        isAiLogged = isAiLogged,
        mealCategory = mealCategory,
        itemsData = itemsData
    )

    companion object {
        fun fromDomain(meal: Meal): MealEntity = MealEntity(
            id = meal.id,
            mealType = meal.mealType,
            imageUrl = meal.imageUrl,
            timestamp = meal.timestamp,
            totalCalories = meal.totalCalories,
            totalProtein = meal.totalProtein,
            totalCarbs = meal.totalCarbs,
            totalFat = meal.totalFat,
            isSynced = meal.isSynced,
            totalFiber = meal.totalFiber,
            totalSugar = meal.totalSugar,
            totalSodium = meal.totalSodium,
            totalPotassium = meal.totalPotassium,
            totalCalcium = meal.totalCalcium,
            totalIron = meal.totalIron,
            totalVitaminC = meal.totalVitaminC,
            totalVitaminD = meal.totalVitaminD,
            rawTextInput = meal.rawTextInput,
            isAiLogged = meal.isAiLogged,
            mealCategory = meal.mealCategory,
            itemsData = meal.itemsData
        )
    }
}
