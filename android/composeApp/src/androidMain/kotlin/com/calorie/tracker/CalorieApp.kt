package com.calorie.tracker

import android.app.Application
import androidx.room.Room
import com.calorie.tracker.core.database.AppDatabase
import com.calorie.tracker.core.network.CalorieApiClient
import com.calorie.tracker.feature_auth.data.AndroidAuthRepository
import com.calorie.tracker.feature_auth.domain.AuthRepository
import com.calorie.tracker.feature_journal.data.local.AndroidBookmarkRepository
import com.calorie.tracker.feature_journal.data.local.AndroidMealRepository
import com.calorie.tracker.feature_journal.domain.BookmarkRepository
import com.calorie.tracker.feature_journal.domain.MealRepository

class CalorieApp : Application() {

    // ── Backend URL — change to your deployed URL for prod ──
    private val BASE_URL = "https://calorie-tracker-backend-878280965690.us-central1.run.app"

    val apiClient: CalorieApiClient by lazy {
        CalorieApiClient(baseUrl = BASE_URL)
    }

    val authRepository: AuthRepository by lazy {
        AndroidAuthRepository(context = applicationContext, apiClient = apiClient)
    }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "calorie_tracker_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6
            )
            .build()
    }

    val mealRepository: MealRepository by lazy {
        AndroidMealRepository(database.mealDao())
    }

    val bookmarkRepository: BookmarkRepository by lazy {
        AndroidBookmarkRepository(database.bookmarkedMealDao())
    }

    val weightRepository: com.calorie.tracker.feature_journal.domain.WeightRepository by lazy {
        com.calorie.tracker.feature_journal.data.local.AndroidWeightRepository(database.weightDao())
    }

    val waterRepository: com.calorie.tracker.feature_journal.domain.WaterRepository by lazy {
        com.calorie.tracker.feature_journal.data.local.AndroidWaterRepository(database.waterDao())
    }
}
