package com.calorie.tracker.feature_journal.domain

import com.calorie.tracker.model.Meal
import kotlinx.coroutines.flow.Flow

interface MealRepository {
    fun getMealsForDate(startOfDay: Long, endOfDay: Long): Flow<List<Meal>>
    suspend fun insertMeal(meal: Meal): Long
    suspend fun getUnsyncedMeals(): List<Meal>
    suspend fun updateMeal(meal: Meal)
    suspend fun deleteMealById(mealId: Long)
}
