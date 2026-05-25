package com.calorie.tracker.controller;

import com.calorie.tracker.dto.WeightLogRequest;
import com.calorie.tracker.dto.WeightLogResponse;
import com.calorie.tracker.model.User;
import com.calorie.tracker.repository.UserRepository;
import com.calorie.tracker.security.CustomUserDetails;
import com.calorie.tracker.service.WeightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeightService weightService;

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(java.util.Map.ofEntries(
                java.util.Map.entry("id", user.getId()),
                java.util.Map.entry("name", user.getName()),
                java.util.Map.entry("email", user.getEmail()),
                java.util.Map.entry("height", user.getHeight() != null ? user.getHeight() : 0.0),
                java.util.Map.entry("weight", user.getCurrentWeight() != null ? user.getCurrentWeight() : 0.0),
                java.util.Map.entry("age", user.getAge() != null ? user.getAge() : 0),
                java.util.Map.entry("lifestyle", user.getLifestyle() != null ? user.getLifestyle().name() : "SEDENTARY"),
                java.util.Map.entry("goal", user.getGoal() != null ? user.getGoal().name() : "MAINTENANCE"),
                java.util.Map.entry("dailyCalorieGoal", user.getDailyCalorieGoal() != null ? user.getDailyCalorieGoal() : 2000),
                java.util.Map.entry("dailyProteinGoal", user.getDailyProteinGoal() != null ? user.getDailyProteinGoal() : 150),
                java.util.Map.entry("dailyCarbsGoal", user.getDailyCarbsGoal() != null ? user.getDailyCarbsGoal() : 200),
                java.util.Map.entry("dailyFatGoal", user.getDailyFatGoal() != null ? user.getDailyFatGoal() : 65)
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody java.util.Map<String, Object> updates) {
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updates.containsKey("name") && updates.get("name") != null) user.setName((String) updates.get("name"));
        if (updates.containsKey("height") && updates.get("height") != null) user.setHeight(((Number) updates.get("height")).doubleValue());
        if (updates.containsKey("weight") && updates.get("weight") != null) user.setCurrentWeight(((Number) updates.get("weight")).doubleValue());
        if (updates.containsKey("age") && updates.get("age") != null) user.setAge(((Number) updates.get("age")).intValue());
        if (updates.containsKey("lifestyle") && updates.get("lifestyle") != null) user.setLifestyle(com.calorie.tracker.model.Lifestyle.valueOf((String) updates.get("lifestyle")));
        if (updates.containsKey("goal") && updates.get("goal") != null) user.setGoal(com.calorie.tracker.model.GoalType.valueOf((String) updates.get("goal")));
        if (updates.containsKey("dailyCalorieGoal") && updates.get("dailyCalorieGoal") != null) user.setDailyCalorieGoal(((Number) updates.get("dailyCalorieGoal")).intValue());
        if (updates.containsKey("dailyProteinGoal") && updates.get("dailyProteinGoal") != null) user.setDailyProteinGoal(((Number) updates.get("dailyProteinGoal")).intValue());
        if (updates.containsKey("dailyCarbsGoal") && updates.get("dailyCarbsGoal") != null) user.setDailyCarbsGoal(((Number) updates.get("dailyCarbsGoal")).intValue());
        if (updates.containsKey("dailyFatGoal") && updates.get("dailyFatGoal") != null) user.setDailyFatGoal(((Number) updates.get("dailyFatGoal")).intValue());

        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Profile updated successfully"));
    }

    @PostMapping("/weight")
    public ResponseEntity<WeightLogResponse> logWeight(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestBody WeightLogRequest request) {
        WeightLogResponse response = weightService.logWeight(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }
}
