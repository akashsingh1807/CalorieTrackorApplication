package com.calorie.tracker.fakes

import com.calorie.tracker.feature_auth.domain.AuthRepository
import com.calorie.tracker.feature_journal.domain.*
import com.calorie.tracker.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAuthRepository : AuthRepository {
    var loggedIn = false
    override suspend fun login(email: String, password: String): Result<String> {
        loggedIn = true
        return Result.success("fake_token")
    }
    override suspend fun loginWithGoogle(idToken: String): Result<String> {
        loggedIn = true
        return Result.success("fake_token")
    }
    override suspend fun register(name: String, email: String, password: String): Result<String> {
        loggedIn = true
        return Result.success("fake_token")
    }
    override fun saveToken(token: String) { loggedIn = true }
    override fun isLoggedIn(): Boolean = loggedIn
    override fun getToken(): String? = if (loggedIn) "fake_token" else null
    override fun clearToken() { loggedIn = false }
}

class FakeMealRepository : MealRepository {
    private val mealsFlow = MutableStateFlow<List<Meal>>(emptyList())
    
    override fun getMealsForDate(startOfDay: Long, endOfDay: Long): Flow<List<Meal>> {
        return mealsFlow // Simplify for tests
    }

    override suspend fun insertMeal(meal: Meal): Long {
        val currentList = mealsFlow.value.toMutableList()
        val newId = (currentList.size + 1).toLong()
        currentList.add(meal.copy(id = newId))
        mealsFlow.value = currentList
        return newId
    }

    override suspend fun getUnsyncedMeals(): List<Meal> {
        return emptyList()
    }

    override suspend fun updateMeal(meal: Meal) {
        val currentList = mealsFlow.value.toMutableList()
        val idx = currentList.indexOfFirst { it.id == meal.id }
        if (idx != -1) {
            currentList[idx] = meal
            mealsFlow.value = currentList
        }
    }
}

class FakeBookmarkRepository : BookmarkRepository {
    private val bookmarksFlow = MutableStateFlow<List<BookmarkedMeal>>(emptyList())

    override fun getAllBookmarks(): Flow<List<BookmarkedMeal>> = bookmarksFlow

    override suspend fun insertBookmark(meal: BookmarkedMeal): Long {
        val currentList = bookmarksFlow.value.toMutableList()
        val newId = (currentList.size + 1).toLong()
        currentList.add(meal.copy(id = newId))
        bookmarksFlow.value = currentList
        return newId
    }

    override suspend fun deleteBookmark(id: Long) {
        val currentList = bookmarksFlow.value.toMutableList()
        currentList.removeAll { it.id == id }
        bookmarksFlow.value = currentList
    }
}

class FakeWeightRepository : WeightRepository {
    private val weightLogs = MutableStateFlow<List<WeightEntry>>(emptyList())

    override fun getAllWeights(): Flow<List<WeightEntry>> = weightLogs

    override suspend fun getLatestWeight(): WeightEntry? {
        return weightLogs.value.maxByOrNull { it.timestamp }
    }

    override suspend fun getWeightByDate(dateStr: String): WeightEntry? {
        return weightLogs.value.find { it.dateStr == dateStr }
    }

    override suspend fun insertWeight(weightKg: Double, dateStr: String, timestamp: Long) {
        val currentList = weightLogs.value.toMutableList()
        currentList.add(WeightEntry(
            id = (currentList.size + 1).toLong(),
            weightKg = weightKg,
            dateStr = dateStr,
            timestamp = timestamp
        ))
        weightLogs.value = currentList
    }

    override suspend fun deleteWeight(id: Long) {
        val currentList = weightLogs.value.toMutableList()
        currentList.removeAll { it.id == id }
        weightLogs.value = currentList
    }
}

class FakeWaterRepository : WaterRepository {
    private val waterLogs = MutableStateFlow<List<WaterEntry>>(emptyList())

    override fun getAllWaterLogs(): Flow<List<WaterEntry>> = waterLogs

    override fun getWaterLogByDate(dateStr: String): Flow<WaterEntry?> {
        return waterLogs.map { list -> list.find { it.dateStr == dateStr } }
    }

    override suspend fun addGlasses(glasses: Int, dateStr: String, timestamp: Long) {
        val currentList = waterLogs.value.toMutableList()
        val index = currentList.indexOfFirst { it.dateStr == dateStr }
        
        if (index != -1) {
            val existing = currentList[index]
            currentList[index] = existing.copy(glasses = existing.glasses + glasses)
        } else {
            currentList.add(WaterEntry(
                id = (currentList.size + 1).toLong(),
                glasses = glasses,
                dateStr = dateStr,
                timestamp = timestamp
            ))
        }
        waterLogs.value = currentList
    }

    override suspend fun deleteWaterLog(id: Long) {
        val currentList = waterLogs.value.toMutableList()
        currentList.removeAll { it.id == id }
        waterLogs.value = currentList
    }
}
