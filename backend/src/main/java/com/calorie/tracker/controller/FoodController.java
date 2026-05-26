package com.calorie.tracker.controller;

import com.calorie.tracker.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import com.calorie.tracker.model.FoodConversionFactor;
import com.calorie.tracker.repository.FoodConversionFactorRepository;
import org.springframework.beans.factory.annotation.Autowired;

// Autowire Gemini fallback service
    @Autowired
    private GeminiFoodInfoService geminiService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFoods(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @RequestParam("q") String query) {
        List<FoodConversionFactor> results = repository.fuzzyFindByName(query);
        if (results.isEmpty()) {
            FoodConversionFactor fetched = geminiService.fetchAndStore(query);
            if (fetched != null) {
                results = List.of(fetched);
            }
        }
        List<Map<String, Object>> foods = results.stream().map(f -> Map.of(
                "id", f.getId(),
                "name", f.getName(),
                "serving", "100g",
                "calories", f.getCalories() != null ? f.getCalories() : BigDecimal.ZERO,
                "protein", f.getProtein() != null ? f.getProtein() : BigDecimal.ZERO,
                "fat", f.getFat() != null ? f.getFat() : BigDecimal.ZERO,
                "carbohydrate", f.getCarbohydrate() != null ? f.getCarbohydrate() : BigDecimal.ZERO
        )).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("foods", foods));
    }

    @GetMapping("/{foodId}")
    public ResponseEntity<Map<String, Object>> getFoodDetails(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @PathVariable Long foodId) {
        return ResponseEntity.ok(Map.of("id", foodId, "name", "Banana"));
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentFoods(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of("foods", List.of()));
    }

    @GetMapping("/favorites")
    public ResponseEntity<Map<String, Object>> getFavoriteFoods(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of("foods", List.of()));
    }

    @PostMapping("/favorites")
    public ResponseEntity<Map<String, Object>> addFavoriteFood(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                               @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(Map.of("success", true, "message", "Added to favorites"));
    }
}
