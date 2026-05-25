package com.calorie.tracker.feature_journal.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorie.tracker.core.network.CalorieApiClient
import com.calorie.tracker.feature_journal.domain.BookmarkRepository
import com.calorie.tracker.feature_journal.domain.MealRepository
import com.calorie.tracker.model.BookmarkedMeal
import com.calorie.tracker.model.FoodItemDto
import com.calorie.tracker.model.Meal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.*

// Represents the state of the meal analysis workflow
sealed class MealAnalysisState {
    object Idle : MealAnalysisState()
    object Analyzing : MealAnalysisState()  // Loading spinner
    data class PendingConfirmation(
        val originalText: String,
        val foodItems: List<FoodItemDto>
    ) : MealAnalysisState()  // Show confirmation dialog
    data class Error(val message: String) : MealAnalysisState()
}

class DashboardViewModel(
    private val mealRepository: MealRepository,
    private val apiClient: CalorieApiClient? = null,
    private val bookmarkRepository: BookmarkRepository? = null
) : ViewModel() {

    private val _meals = MutableStateFlow<List<Meal>>(emptyList())
    val meals: StateFlow<List<Meal>> = _meals.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate>(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _analysisState = MutableStateFlow<MealAnalysisState>(MealAnalysisState.Idle)
    val analysisState: StateFlow<MealAnalysisState> = _analysisState.asStateFlow()

    // ── Bookmarks ───────────────────────────────────────
    private val _bookmarks = MutableStateFlow<List<BookmarkedMeal>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkedMeal>> = _bookmarks.asStateFlow()

    // ── Streak State ─────────────────────────────────────
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _longestStreak = MutableStateFlow(0)
    val longestStreak: StateFlow<Int> = _longestStreak.asStateFlow()

    /** Snackbar-style feedback message after quick actions */
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedDate.collectLatest { date ->
                val timeZone = TimeZone.currentSystemDefault()
                val startOfDay = LocalDateTime(
                    year = date.year,
                    monthNumber = date.monthNumber,
                    dayOfMonth = date.dayOfMonth,
                    hour = 0,
                    minute = 0,
                    second = 0,
                    nanosecond = 0
                ).toInstant(timeZone).toEpochMilliseconds()

                val endOfDay = startOfDay + 24 * 60 * 60 * 1000L

                mealRepository.getMealsForDate(startOfDay, endOfDay).collectLatest { dailyMeals ->
                    _meals.value = dailyMeals
                }
            }
        }

        // Calculate streak dynamically from all meals
        viewModelScope.launch {
            val timeZone = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(timeZone).date
            val startOfPeriod = today.minus(90, kotlinx.datetime.DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()
            val endOfPeriod = today.plus(1, kotlinx.datetime.DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds()

            mealRepository.getMealsForDate(startOfPeriod, endOfPeriod).collectLatest { periodMeals ->
                val (curr, long) = calculateStreak(periodMeals, today, timeZone)
                _streak.value = curr
                _longestStreak.value = long
            }
        }

        // Observe bookmarks live
        viewModelScope.launch {
            bookmarkRepository?.getAllBookmarks()?.collectLatest { list ->
                _bookmarks.value = list
            }
        }
    }

    private fun calculateStreak(meals: List<Meal>, today: LocalDate, timeZone: TimeZone): Pair<Int, Int> {
        val loggedDates = meals.map {
            kotlinx.datetime.Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(timeZone).date
        }.toSet()

        var current = 0
        var longest = 0
        var tempStreak = 0

        val hasLoggedToday = loggedDates.contains(today)
        val hasLoggedYesterday = loggedDates.contains(today.minus(1, kotlinx.datetime.DateTimeUnit.DAY))
        val startCheckingFrom = if (hasLoggedToday) today else if (hasLoggedYesterday) today.minus(1, kotlinx.datetime.DateTimeUnit.DAY) else null

        if (startCheckingFrom != null) {
            var checkDate: LocalDate = startCheckingFrom
            while (loggedDates.contains(checkDate)) {
                current++
                checkDate = checkDate.minus(1, kotlinx.datetime.DateTimeUnit.DAY)
            }
        }

        if (loggedDates.isNotEmpty()) {
            val sortedDates = loggedDates.sorted()
            var lastDate: LocalDate? = null
            for (date in sortedDates) {
                if (lastDate == null) {
                    tempStreak = 1
                } else {
                    val daysBetween = lastDate.daysUntil(date)
                    if (daysBetween == 1) {
                        tempStreak++
                    } else if (daysBetween > 1) {
                        longest = maxOf(longest, tempStreak)
                        tempStreak = 1
                    }
                }
                lastDate = date
            }
            longest = maxOf(longest, tempStreak)
        }

        return Pair(current, maxOf(longest, current))
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    /**
     * Analyzes the user's text input via Gemini AI to extract accurate food items
     * with per-quantity nutrition. Shows a confirmation dialog before logging.
     */
    fun analyzeAndLogMeal(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _analysisState.value = MealAnalysisState.Analyzing
            try {
                if (apiClient != null) {
                    val result = apiClient.analyzeText(text)
                    result.onSuccess { response ->
                        if (response.foodItems.isNotEmpty()) {
                            _analysisState.value = MealAnalysisState.PendingConfirmation(
                                originalText = text,
                                foodItems = response.foodItems
                            )
                        } else {
                            // Fallback if AI returned empty
                            val fallback = buildFallbackItem(text)
                            if (fallback != null) {
                                _analysisState.value = MealAnalysisState.PendingConfirmation(
                                    originalText = text,
                                    foodItems = listOf(fallback)
                                )
                            } else {
                                _analysisState.value = MealAnalysisState.Error("Could not recognize food. Please try a different spelling.")
                            }
                        }
                    }.onFailure {
                        // Fallback to local parse on network error
                        val fallback = buildFallbackItem(text)
                        if (fallback != null) {
                            _analysisState.value = MealAnalysisState.PendingConfirmation(
                                originalText = text,
                                foodItems = listOf(fallback)
                            )
                        } else {
                            _analysisState.value = MealAnalysisState.Error("Network error. Connect to internet for full food database.")
                        }
                    }
                } else {
                    // No API client (test/local mode), use local parser
                    val fallback = buildFallbackItem(text)
                    if (fallback != null) {
                        _analysisState.value = MealAnalysisState.PendingConfirmation(
                            originalText = text,
                            foodItems = listOf(fallback)
                        )
                    } else {
                        _analysisState.value = MealAnalysisState.Error("Could not recognize food locally.")
                    }
                }
            } catch (e: Exception) {
                _analysisState.value = MealAnalysisState.Error("Analysis failed: ${e.message}")
            }
        }
    }

    fun analyzeAndLogMealFromImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _analysisState.value = MealAnalysisState.Analyzing
            try {
                if (apiClient != null) {
                    val result = apiClient.analyzeMealImage(imageBytes)
                    result.onSuccess { response ->
                        if (response.foodItems.isNotEmpty()) {
                            _analysisState.value = MealAnalysisState.PendingConfirmation(
                                originalText = "📷 Photo Logged",
                                foodItems = response.foodItems
                            )
                        } else {
                            _analysisState.value = MealAnalysisState.Error("Could not recognize food in the image.")
                        }
                    }.onFailure {
                        _analysisState.value = MealAnalysisState.Error("Network error: ${it.message}")
                    }
                } else {
                    _analysisState.value = MealAnalysisState.Error("API client not configured for image upload.")
                }
            } catch (e: Exception) {
                _analysisState.value = MealAnalysisState.Error("Analysis failed: ${e.message}")
            }
        }
    }

    /** Called when user confirms the food items in the dialog */
    fun confirmAndLogMeals(foodItems: List<FoodItemDto>, originalText: String? = null, saveAsBookmark: Boolean = false, bookmarkName: String = "") {
        viewModelScope.launch {
            val timeZone = TimeZone.currentSystemDefault()
            val date = _selectedDate.value
            val timestamp = LocalDateTime(
                year = date.year,
                monthNumber = date.monthNumber,
                dayOfMonth = date.dayOfMonth,
                hour = 12,
                minute = 0,
                second = 0,
                nanosecond = 0
            ).toInstant(timeZone).toEpochMilliseconds()

            val totalCalories = foodItems.sumOf { it.calories }
            val totalProtein = foodItems.sumOf { it.protein }
            val totalCarbs = foodItems.sumOf { it.carbs }
            val totalFat = foodItems.sumOf { it.fat }

            // Build a descriptive meal name from all items
            val mealType = foodItems.joinToString(", ") { it.name }

            val meal = Meal(
                id = 0,
                mealType = mealType,
                imageUrl = null,
                timestamp = timestamp,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                isSynced = false,
                totalFiber = foodItems.sumOf { it.fiber },
                totalSugar = foodItems.sumOf { it.sugar },
                totalSodium = foodItems.sumOf { it.sodium },
                totalPotassium = foodItems.sumOf { it.potassium },
                totalCalcium = foodItems.sumOf { it.calcium },
                totalIron = foodItems.sumOf { it.iron },
                totalVitaminC = foodItems.sumOf { it.vitaminC },
                totalVitaminD = foodItems.sumOf { it.vitaminD },
                rawTextInput = originalText,
                isAiLogged = originalText != null
            )
            mealRepository.insertMeal(meal)

            // Optionally save as bookmark
            if (saveAsBookmark) {
                val name = bookmarkName.ifBlank { mealType.take(30) }
                saveBookmark(name, foodItems)
            }

            _analysisState.value = MealAnalysisState.Idle
        }
    }

    fun dismissAnalysis() {
        _analysisState.value = MealAnalysisState.Idle
    }

    // ── Bookmark operations ────────────────────────────────────────────────

    /** Save a list of food items as a named bookmark */
    fun saveBookmark(name: String, foodItems: List<FoodItemDto>) {
        viewModelScope.launch {
            val entity = BookmarkedMeal(
                name = name,
                totalCalories = foodItems.sumOf { it.calories },
                totalProtein = foodItems.sumOf { it.protein },
                totalCarbs = foodItems.sumOf { it.carbs },
                totalFat = foodItems.sumOf { it.fat },
                itemsData = BookmarkedMeal.serialiseItems(foodItems)
            )
            bookmarkRepository?.insertBookmark(entity)
            _feedbackMessage.value = "\"$name\" saved to bookmarks ★"
        }
    }

    /** Save an already-logged meal (from the meals list) as a bookmark */
    fun saveLoggedMealAsBookmark(meal: Meal) {
        viewModelScope.launch {
            val entity = BookmarkedMeal(
                name = meal.mealType.take(40),
                totalCalories = meal.totalCalories,
                totalProtein = meal.totalProtein,
                totalCarbs = meal.totalCarbs,
                totalFat = meal.totalFat,
                itemsData = BookmarkedMeal.serialiseItems(
                    listOf(
                        FoodItemDto(
                            name = meal.mealType,
                            servingSize = "1 serving",
                            calories = meal.totalCalories,
                            protein = meal.totalProtein,
                            carbs = meal.totalCarbs,
                            fat = meal.totalFat,
                            fiber = meal.totalFiber,
                            sugar = meal.totalSugar,
                            sodium = meal.totalSodium,
                            potassium = meal.totalPotassium,
                            calcium = meal.totalCalcium,
                            iron = meal.totalIron,
                            vitaminC = meal.totalVitaminC,
                            vitaminD = meal.totalVitaminD
                        )
                    )
                )
            )
            bookmarkRepository?.insertBookmark(entity)
            _feedbackMessage.value = "Saved to bookmarks ★"
        }
    }

    /** Instantly log a bookmarked meal without AI analysis */
    fun logBookmark(bookmark: BookmarkedMeal) {
        viewModelScope.launch {
            val timeZone = TimeZone.currentSystemDefault()
            val date = _selectedDate.value
            val timestamp = LocalDateTime(
                year = date.year,
                monthNumber = date.monthNumber,
                dayOfMonth = date.dayOfMonth,
                hour = 12,
                minute = 0,
                second = 0,
                nanosecond = 0
            ).toInstant(timeZone).toEpochMilliseconds()

            // Reconstruct food items from bookmark to re-aggregate micronutrients
            val items = BookmarkedMeal.deserialiseItems(bookmark.itemsData)
            val meal = Meal(
                id = 0,
                mealType = bookmark.name,
                imageUrl = null,
                timestamp = timestamp,
                totalCalories = bookmark.totalCalories,
                totalProtein = bookmark.totalProtein,
                totalCarbs = bookmark.totalCarbs,
                totalFat = bookmark.totalFat,
                isSynced = false,
                totalFiber = items.sumOf { it.fiber },
                totalSugar = items.sumOf { it.sugar },
                totalSodium = items.sumOf { it.sodium },
                totalPotassium = items.sumOf { it.potassium },
                totalCalcium = items.sumOf { it.calcium },
                totalIron = items.sumOf { it.iron },
                totalVitaminC = items.sumOf { it.vitaminC },
                totalVitaminD = items.sumOf { it.vitaminD }
            )
            mealRepository.insertMeal(meal)
            _feedbackMessage.value = "\"${bookmark.name}\" logged ✓"
        }
    }

    /** Delete a bookmark */
    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            bookmarkRepository?.deleteBookmark(id)
            _feedbackMessage.value = "Bookmark removed"
        }
    }

    fun logMeal(mealType: String, calories: Double, protein: Double, carbs: Double, fat: Double, date: LocalDate) {
        viewModelScope.launch {
            val timeZone = TimeZone.currentSystemDefault()
            val startOfSelectedDay = LocalDateTime(
                year = date.year,
                monthNumber = date.monthNumber,
                dayOfMonth = date.dayOfMonth,
                hour = 12,
                minute = 0,
                second = 0,
                nanosecond = 0
            ).toInstant(timeZone).toEpochMilliseconds()

            val meal = Meal(
                id = 0,
                mealType = mealType,
                imageUrl = null,
                timestamp = startOfSelectedDay,
                totalCalories = calories,
                totalProtein = protein,
                totalCarbs = carbs,
                totalFat = fat,
                isSynced = false
            )
            mealRepository.insertMeal(meal)
        }
    }

    /**
     * Local fallback parser — used when network is unavailable.
     * Uses per-100g nutrient tables for common Indian and global foods,
     * now including micronutrients (fiber, sugar, sodium, potassium, calcium, iron, vitaminC, vitaminD).
     */
    private fun buildFallbackItem(query: String): FoodItemDto? {
        val lower = query.lowercase().trim()
        val normalizedQuery = lower
            .replace("aalo", "aloo")
            .replace("chiken", "chicken")
            .replace("daal", "dal")
            .replace("chapaty", "chapati")
            .replace("pratha", "paratha")

        // Try to extract quantity from string like "200g chicken" or "2 eggs"
        val quantityGrams = extractGrams(normalizedQuery)
        val quantityPieces = extractPieces(normalizedQuery)

        // Per-100g data: cal, protein, carbs, fat, fiber, sugar, sodium(mg), potassium(mg), calcium(mg), iron(mg), vitC(mg), vitD(µg)
        data class NutrientPer100g(
            val cal: Double, val prot: Double, val carbs: Double, val fat: Double,
            val fiber: Double, val sugar: Double, val sodium: Double, val potassium: Double,
            val calcium: Double, val iron: Double, val vitaminC: Double, val vitaminD: Double
        )

        val foodDb = mapOf(
            "banana"       to NutrientPer100g(89.0,  1.1, 23.0,  0.3,  2.6, 12.2,  1.0,  358.0,  5.0, 0.3,  8.7,  0.0),
            "apple"        to NutrientPer100g(52.0,  0.3, 14.0,  0.2,  2.4, 10.4,  1.0,  107.0,  6.0, 0.1,  4.6,  0.0),
            "egg"          to NutrientPer100g(155.0, 13.0, 1.1, 11.0,  0.0,  1.1, 124.0, 126.0, 50.0, 1.8,  0.0,  2.0),
            "chicken"      to NutrientPer100g(165.0, 31.0, 0.0,  3.6,  0.0,  0.0,  74.0, 256.0, 15.0, 1.0,  0.0,  0.1),
            "rice"         to NutrientPer100g(130.0,  2.7, 28.0, 0.3,  0.4,  0.0,   1.0,  35.0, 10.0, 0.2,  0.0,  0.0),
            "brown rice"   to NutrientPer100g(112.0,  2.6, 24.0, 0.9,  1.8,  0.4,   4.0,  79.0, 10.0, 0.5,  0.0,  0.0),
            "dal"          to NutrientPer100g(115.0,  7.0, 18.0, 0.5,  3.5,  1.5,  10.0, 210.0, 30.0, 1.8,  1.5,  0.0),
            "daal"         to NutrientPer100g(115.0,  7.0, 18.0, 0.5,  3.5,  1.5,  10.0, 210.0, 30.0, 1.8,  1.5,  0.0),
            "dahi"         to NutrientPer100g(61.0,   5.0,  3.4, 3.3,  0.0,  3.2,  46.0, 141.0,110.0, 0.1,  0.5,  0.1),
            "yogurt"       to NutrientPer100g(61.0,   5.0,  3.4, 3.3,  0.0,  3.2,  46.0, 141.0,110.0, 0.1,  0.5,  0.1),
            "milk"         to NutrientPer100g(61.0,   3.2,  4.8, 3.3,  0.0,  5.0,  44.0, 132.0,113.0, 0.0,  0.5,  1.3),
            "ghee"         to NutrientPer100g(900.0,  0.0,  0.0,99.8,  0.0,  0.0,   2.0,   5.0,  4.0, 0.0,  0.0,  0.0),
            "aloo paratha" to NutrientPer100g(250.0,  5.0, 36.0,  9.0,  3.0,  1.0, 350.0, 180.0, 40.0, 1.8,  2.0,  0.0),
            "paratha"      to NutrientPer100g(290.0,  6.0, 40.0, 12.0,  2.5,  1.0, 380.0, 150.0, 30.0, 1.5,  0.0,  0.0),
            "roti"         to NutrientPer100g(297.0,  9.9, 52.0, 4.1,  3.5,  1.0, 320.0, 190.0, 70.0, 2.5,  0.0,  0.0),
            "chapati"      to NutrientPer100g(297.0,  9.9, 52.0, 4.1,  3.5,  1.0, 320.0, 190.0, 70.0, 2.5,  0.0,  0.0),
            "paneer"       to NutrientPer100g(296.0, 18.3,  1.2,22.7,  0.0,  0.5,  32.0,  40.0,480.0, 0.5,  0.0,  0.4),
            "bread"        to NutrientPer100g(265.0,  9.0, 49.0, 3.2,  2.7,  5.0, 491.0, 100.0, 80.0, 3.0,  0.0,  0.0),
            "oats"         to NutrientPer100g(389.0, 17.0, 66.0, 7.0, 10.6,  0.0,   2.0, 429.0, 54.0, 4.7,  0.0,  0.0),
            "potato"       to NutrientPer100g(77.0,   2.0, 17.0, 0.1,  2.2,  0.8,   6.0, 421.0, 12.0, 0.8, 19.7,  0.0),
            "salmon"       to NutrientPer100g(208.0, 20.0,  0.0,13.0,  0.0,  0.0,  59.0, 363.0, 12.0, 0.8,  3.0, 14.0),
            "tuna"         to NutrientPer100g(132.0, 28.0,  0.0, 1.3,  0.0,  0.0,  50.0, 252.0, 38.0, 1.0,  0.0,  5.0),
            "almonds"      to NutrientPer100g(579.0, 21.0, 22.0,50.0, 12.5,  4.4,   1.0, 733.0,264.0, 3.7,  0.0,  0.0),
            "peanut"       to NutrientPer100g(567.0, 26.0, 16.0,49.0,  8.5,  4.7,  18.0, 705.0, 92.0, 4.6,  0.0,  0.0)
        )

        var matchedNutrients: NutrientPer100g? = null
        var matchedFood = ""
        for ((food, nutrients) in foodDb) {
            if (normalizedQuery.contains(food)) {
                matchedNutrients = nutrients
                matchedFood = food
                break
            }
        }

        if (matchedNutrients != null) {
            val grams = quantityGrams ?: when {
                matchedFood == "egg" -> (quantityPieces ?: 1) * 50.0
                matchedFood in listOf("roti", "chapati") -> (quantityPieces ?: 1) * 40.0
                matchedFood == "banana" -> (quantityPieces ?: 1) * 120.0
                matchedFood == "apple" -> (quantityPieces ?: 1) * 182.0
                matchedFood == "aloo paratha" -> (quantityPieces ?: 1) * 150.0
                matchedFood == "paratha" -> (quantityPieces ?: 1) * 100.0
                else -> 100.0
            }
            val ratio = grams / 100.0
            return FoodItemDto(
                name = "${matchedFood.replaceFirstChar { it.uppercase() }} (${grams.toInt()}g)",
                servingSize = "${"%.0f".format(grams)}g",
                calories   = "%.1f".format(matchedNutrients.cal       * ratio).toDouble(),
                protein    = "%.1f".format(matchedNutrients.prot      * ratio).toDouble(),
                carbs      = "%.1f".format(matchedNutrients.carbs     * ratio).toDouble(),
                fat        = "%.1f".format(matchedNutrients.fat       * ratio).toDouble(),
                fiber      = "%.1f".format(matchedNutrients.fiber     * ratio).toDouble(),
                sugar      = "%.1f".format(matchedNutrients.sugar     * ratio).toDouble(),
                sodium     = "%.1f".format(matchedNutrients.sodium    * ratio).toDouble(),
                potassium  = "%.1f".format(matchedNutrients.potassium * ratio).toDouble(),
                calcium    = "%.1f".format(matchedNutrients.calcium   * ratio).toDouble(),
                iron       = "%.1f".format(matchedNutrients.iron      * ratio).toDouble(),
                vitaminC   = "%.1f".format(matchedNutrients.vitaminC  * ratio).toDouble(),
                vitaminD   = "%.1f".format(matchedNutrients.vitaminD  * ratio).toDouble()
            )
        }

        return null
    }

    private fun extractGrams(text: String): Double? {
        val regex = Regex("""(\d+(?:\.\d+)?)\s*(?:g|gm|gram|grams)""")
        return regex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun extractPieces(text: String): Int? {
        val regex = Regex("""^(\d+)\s+""")
        return regex.find(text)?.groupValues?.get(1)?.toIntOrNull()
    }
}

