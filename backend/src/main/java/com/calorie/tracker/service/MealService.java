package com.calorie.tracker.service;

import com.calorie.tracker.dto.FoodItemDto;
import com.calorie.tracker.dto.MealRequest;
import com.calorie.tracker.dto.MealResponse;
import com.calorie.tracker.model.FoodItem;
import com.calorie.tracker.model.Meal;
import com.calorie.tracker.model.User;
import com.calorie.tracker.repository.MealRepository;
import com.calorie.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeminiVisionService geminiVisionService;

    @Transactional
    public MealResponse saveMeal(Long userId, MealRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime mealTimestamp = request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now();

        Meal meal = Meal.builder()
                .user(user)
                .mealType(request.getMealType())
                .imageUrl(request.getImageUrl())
                .timestamp(mealTimestamp)
                .build();

        double totalCal = 0, totalPro = 0, totalCarb = 0, totalFat = 0;

        if (request.getFoodItems() != null) {
            for (FoodItemDto dto : request.getFoodItems()) {
                FoodItem item = FoodItem.builder()
                        .meal(meal)
                        .name(dto.getName())
                        .servingSize(dto.getServingSize())
                        .calories(dto.getCalories())
                        .protein(dto.getProtein())
                        .carbs(dto.getCarbs())
                        .fat(dto.getFat())
                        .build();

                meal.getFoodItems().add(item);
                
                totalCal += dto.getCalories() != null ? dto.getCalories() : 0;
                totalPro += dto.getProtein() != null ? dto.getProtein() : 0;
                totalCarb += dto.getCarbs() != null ? dto.getCarbs() : 0;
                totalFat += dto.getFat() != null ? dto.getFat() : 0;

                // Cache the item (HealthifyMe mechanism)
                try {
                    String persona = user.getPersonaPreference() != null ? user.getPersonaPreference() : "NONE";
                    geminiVisionService.cacheFoodItem(persona, dto);
                } catch (Exception e) {
                    org.slf4j.LoggerFactory.getLogger(MealService.class)
                            .error("HealthifyMe Cache: Failed to cache manual food log: {}", dto.getName(), e);
                }
            }
        }

        meal.setTotalCalories(totalCal);
        meal.setTotalProtein(totalPro);
        meal.setTotalCarbs(totalCarb);
        meal.setTotalFat(totalFat);

        Meal savedMeal = mealRepository.save(meal);
        return mapToResponse(savedMeal);
    }

    public List<MealResponse> getDailyMeals(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        List<Meal> meals = mealRepository.findMealsByUserAndDate(userId, startOfDay, endOfDay);
        return meals.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private MealResponse mapToResponse(Meal meal) {
        List<FoodItemDto> items = meal.getFoodItems().stream().map(fi -> {
            FoodItemDto dto = new FoodItemDto();
            dto.setName(fi.getName());
            dto.setServingSize(fi.getServingSize());
            dto.setCalories(fi.getCalories());
            dto.setProtein(fi.getProtein());
            dto.setCarbs(fi.getCarbs());
            dto.setFat(fi.getFat());
            return dto;
        }).collect(Collectors.toList());

        return MealResponse.builder()
                .id(meal.getId())
                .mealType(meal.getMealType())
                .imageUrl(meal.getImageUrl())
                .timestamp(meal.getTimestamp())
                .totalCalories(meal.getTotalCalories())
                .totalProtein(meal.getTotalProtein())
                .totalCarbs(meal.getTotalCarbs())
                .totalFat(meal.getTotalFat())
                .foodItems(items)
                .build();
    }
}
