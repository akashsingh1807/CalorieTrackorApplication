package com.calorie.tracker.repository;

import com.calorie.tracker.model.FoodInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FoodInfoRepository extends JpaRepository<FoodInfo, Long> {
    Optional<FoodInfo> findBySourceId(String sourceId);
    Optional<FoodInfo> findByNameIgnoreCase(String name);
}
