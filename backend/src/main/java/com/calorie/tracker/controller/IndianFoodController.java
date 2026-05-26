package com.calorie.tracker.controller;

import com.calorie.tracker.model.IndianFood;
import com.calorie.tracker.repository.IndianFoodRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Indian Food", description = "Endpoints for Indian food data")
@RestController
@RequestMapping("/api/indian-food")
public class IndianFoodController {

    private final IndianFoodRepository repository;

    @Autowired
    public IndianFoodController(IndianFoodRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "Get paginated list of Indian foods",
            description = "Supports optional name search and max calories filter")
    @GetMapping
    public ResponseEntity<Page<IndianFood>> getIndianFoods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal maxCalories) {
        Pageable pageable = PageRequest.of(page, size);
        Page<IndianFood> result;
        if (name != null && !name.isBlank()) {
            result = repository.findByNameContainingIgnoreCase(name, pageable);
        } else if (maxCalories != null) {
            result = repository.findByCaloriesLessThanEqual(maxCalories, pageable);
        } else {
            result = repository.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }
}
