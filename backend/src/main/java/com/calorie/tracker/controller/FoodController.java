package com.calorie.tracker.controller;

import com.calorie.tracker.model.FoodConversionFactor;
import com.calorie.tracker.repository.FoodConversionFactorRepository;
import com.calorie.tracker.security.CustomUserDetails;
import com.calorie.tracker.service.GeminiFoodInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/food")
public class FoodController {

    @Autowired
    private FoodConversionFactorRepository repository;

    @Autowired
    private GeminiFoodInfoService geminiService;

    @GetMapping("/search")
    public ResponseEntity<Map<String,Object>> searchFoods(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @RequestParam("q") String query) {
        String trimmed = query.trim();
        // 1️⃣ Exact phrase match
        List<FoodConversionFactor> results = repository.findExact(trimmed);
        // 2️⃣ Simple containment (case‑insensitive)
        if (results.isEmpty()) {
            results = repository.findByNameContainingIgnoreCase(trimmed);
        }
        // 3️⃣ Fuzzy search using LIKE, then prioritize entries containing all words
        if (results.isEmpty()) {
            results = repository.fuzzyFindByName(trimmed);
            // Split query into words and keep only those that are non‑empty
            List<String> words = Arrays.stream(trimmed.toLowerCase().split("\\s+"))
                                       .filter(w -> !w.isEmpty())
                                       .toList();
            List<FoodConversionFactor> wordFiltered = results.stream()
                .filter(f -> {
                    String nameLower = f.getName() != null ? f.getName().toLowerCase() : "";
                    return words.stream().allMatch(nameLower::contains);
                })
                .toList();
            if (!wordFiltered.isEmpty()) {
                results = wordFiltered;
            }
        }
        // 4️⃣ Fallback to Gemini service if nothing found
        if (results.isEmpty()) {
            FoodConversionFactor fetched = geminiService.fetchAndStore(trimmed);
            if (fetched != null) {
                results = List.of(fetched);
            }
        }
        List<Map<String,Object>> foods = results.stream()
            .map(f -> Map.of(
                "id", f.getId(),
                "name", f.getName(),
                "serving", "100g",
                "calories", f.getCalories() != null ? f.getCalories() : BigDecimal.ZERO,
                "protein", f.getProtein() != null ? f.getProtein() : BigDecimal.ZERO,
                "fat", f.getFat() != null ? f.getFat() : BigDecimal.ZERO,
                "carbohydrate", f.getCarbohydrate() != null ? f.getCarbohydrate() : BigDecimal.ZERO
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(Map.<String, Object>of("foods", foods));
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
