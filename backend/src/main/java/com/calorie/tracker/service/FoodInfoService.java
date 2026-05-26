package com.calorie.tracker.service;

import com.calorie.tracker.model.FoodInfo;
import com.calorie.tracker.model.FoodItemDto;
import com.calorie.tracker.repository.FoodInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FoodInfoService {
    private static final Logger logger = LoggerFactory.getLogger(FoodInfoService.class);

    @Autowired
    private FoodInfoRepository foodInfoRepository;

    /**
     * Find food info by exact name (case‑insensitive).
     * Returns a single FoodItemDto if found, otherwise empty list.
     */
    public List<FoodItemDto> findFoodByName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        try {
            Optional<FoodInfo> opt = foodInfoRepository.findByNameIgnoreCase(name.trim());
            if (opt.isPresent()) {
                FoodInfo info = opt.get();
                FoodItemDto dto = mapToDto(info);
                return List.of(dto);
            }
        } catch (Exception e) {
            logger.error("Error fetching FoodInfo for name '{}': {}", name, e.getMessage(), e);
        }
        return List.of();
    }

    private FoodItemDto mapToDto(FoodInfo info) {
        FoodItemDto dto = new FoodItemDto();
        dto.setName(info.getName());
        dto.setServingSize("100g"); // stored per 100 g by default
        dto.setCalories(info.getCalories() != null ? info.getCalories() : 0.0);
        dto.setProtein(info.getProtein() != null ? info.getProtein() : 0.0);
        dto.setCarbs(info.getCarbs() != null ? info.getCarbs() : 0.0);
        dto.setFat(info.getFat() != null ? info.getFat() : 0.0);
        dto.setFiber(info.getFiber() != null ? info.getFiber() : 0.0);
        dto.setSugar(info.getSugar() != null ? info.getSugar() : 0.0);
        dto.setSodium(info.getSodium() != null ? info.getSodium() : 0.0);
        dto.setPotassium(info.getPotassium() != null ? info.getPotassium() : 0.0);
        dto.setCalcium(info.getCalcium() != null ? info.getCalcium() : 0.0);
        dto.setIron(info.getIron() != null ? info.getIron() : 0.0);
        dto.setVitaminC(info.getVitaminC() != null ? info.getVitaminC() : 0.0);
        dto.setVitaminD(info.getVitaminD() != null ? info.getVitaminD() : 0.0);
        return dto;
    }
}
