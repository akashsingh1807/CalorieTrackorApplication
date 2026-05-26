package com.calorie.tracker.feature_journal.data.local

import com.calorie.tracker.feature_journal.domain.MealRepository
import com.calorie.tracker.model.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AndroidMealRepository(
    private val mealDao: MealDao
) : MealRepository {

    override fun getMealsForDate(startOfDay: Long, endOfDay: Long): Flow<List<Meal>> {
        return mealDao.getMealsForDate(startOfDay, endOfDay).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMeal(meal: Meal): Long {
        return mealDao.insertMeal(MealEntity.fromDomain(meal))
    }

    override suspend fun getUnsyncedMeals(): List<Meal> {
        return mealDao.getUnsyncedMeals().map { it.toDomain() }
    }

    override suspend fun updateMeal(meal: Meal) {
        mealDao.updateMeal(MealEntity.fromDomain(meal))
    }

    override suspend fun deleteMealById(mealId: Long) {
        mealDao.deleteMealById(mealId)
    }
}
