package com.calorie.tracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.calorie.tracker.feature_journal.data.local.BookmarkedMealDao
import com.calorie.tracker.feature_journal.data.local.BookmarkedMealEntity
import com.calorie.tracker.feature_journal.data.local.MealDao
import com.calorie.tracker.feature_journal.data.local.MealEntity
import com.calorie.tracker.feature_journal.data.local.WaterDao
import com.calorie.tracker.feature_journal.data.local.WaterEntity
import com.calorie.tracker.feature_journal.data.local.WeightDao
import com.calorie.tracker.feature_journal.data.local.WeightEntity

@Database(
    entities = [
        MealEntity::class,
        BookmarkedMealEntity::class,
        WeightEntity::class,
        WaterEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun bookmarkedMealDao(): BookmarkedMealDao
    abstract fun weightDao(): WeightDao
    abstract fun waterDao(): WaterDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `bookmarked_meals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `totalCalories` REAL NOT NULL,
                        `totalProtein` REAL NOT NULL,
                        `totalCarbs` REAL NOT NULL,
                        `totalFat` REAL NOT NULL,
                        `itemsData` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /** Migration 2 → 3: Add 8 micronutrient columns to the meals table */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meals ADD COLUMN totalFiber REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalSugar REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalSodium REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalPotassium REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalCalcium REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalIron REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalVitaminC REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE meals ADD COLUMN totalVitaminD REAL NOT NULL DEFAULT 0.0")
            }
        }

        /** Migration 3 → 4: Add rawTextInput and isAiLogged for WhatsApp-style chat UI */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meals ADD COLUMN rawTextInput TEXT")
                db.execSQL("ALTER TABLE meals ADD COLUMN isAiLogged INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** Migration 4 → 5: Add weight_logs and water_logs tables */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `weight_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `weightKg` REAL NOT NULL,
                        `dateStr` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `water_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `glasses` INTEGER NOT NULL,
                        `dateStr` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Migration 5 → 6: Add mealCategory and itemsData columns to meals table
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE meals ADD COLUMN mealCategory TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE meals ADD COLUMN itemsData TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}
