package com.calorie.tracker.flows

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.calorie.tracker.App
import com.calorie.tracker.fakes.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class DashboardFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testDashboard_AddMealFlow() {
        val authRepository = FakeAuthRepository()
        authRepository.loggedIn = true // Skip auth

        val mealRepository = FakeMealRepository()

        // Profile returns height > 0 to skip onboarding
        val mockProfile = com.calorie.tracker.model.UserProfile(
            id = 1L, name = "Test", email = "test@example.com",
            height = 180.0, weight = 75.0, age = 25, lifestyle = "SEDENTARY", goal = "MAINTAIN",
            dailyCalorieGoal = 2000, dailyProteinGoal = 100, dailyCarbsGoal = 200, dailyFatGoal = 60
        )

        val apiClient = io.mockk.mockk<com.calorie.tracker.core.network.CalorieApiClient>(relaxed = true)
        io.mockk.coEvery { apiClient.getProfile() } returns Result.success(mockProfile)
        
        // Mock the AI text analysis response
        val mockFoodItems = listOf(
            com.calorie.tracker.model.FoodItemDto(
                name = "Apple",
                servingSize = "1 large",
                calories = 95.0,
                protein = 0.5,
                carbs = 25.0,
                fat = 0.3
            )
        )
        val mockAnalysisResponse = com.calorie.tracker.model.AnalyzeTextResponse(
            foodItems = mockFoodItems,
            totalCalories = 95.0
        )
        io.mockk.coEvery { apiClient.analyzeText(any()) } returns Result.success(mockAnalysisResponse)

        composeTestRule.setContent {
            App(
                authRepository = authRepository,
                mealRepository = mealRepository,
                apiClient = apiClient,
                bookmarkRepository = FakeBookmarkRepository(),
                weightRepository = FakeWeightRepository(),
                waterRepository = FakeWaterRepository()
            )
        }

        // Wait for dashboard to load
        composeTestRule.waitForIdle()

        // Check if "What did you eat?" input exists
        val inputField = composeTestRule.onNodeWithText("What did you eat or exercise?")
        inputField.assertExists()

        // Enter food
        inputField.performTextInput("1 large apple")
        
        // Click send/submit button
        composeTestRule.onNodeWithContentDescription("Send").performClick()
        
        // Wait for the UI to update
        composeTestRule.waitForIdle()
        
        // Verify the logged food item is displayed on the screen
        composeTestRule.onNodeWithText("Apple").assertExists()
        composeTestRule.onNodeWithText("95.0").assertExists()
        composeTestRule.onNodeWithText("95 kcal").assertExists()
    }
}
