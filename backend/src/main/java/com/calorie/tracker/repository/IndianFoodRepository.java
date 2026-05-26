package com.calorie.tracker.repository;

import com.calorie.tracker.model.IndianFood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface IndianFoodRepository extends JpaRepository<IndianFood, Long> {
    Optional<IndianFood> findByName(String name);

    // Pagination with name search (case‑insensitive)
    Page<IndianFood> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Pagination with max calories filter
    Page<IndianFood> findByCaloriesLessThanEqual(BigDecimal maxCalories, Pageable pageable);
}
