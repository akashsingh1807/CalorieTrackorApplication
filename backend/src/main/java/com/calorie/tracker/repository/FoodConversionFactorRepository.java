package com.calorie.tracker.repository;

import com.calorie.tracker.model.FoodConversionFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FoodConversionFactorRepository extends JpaRepository<FoodConversionFactor, Long> {
    List<FoodConversionFactor> findByNameContainingIgnoreCase(String name);
    // Additional query methods can be added here if needed
    @Query("SELECT f FROM FoodConversionFactor f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<FoodConversionFactor> fuzzyFindByName(@Param("name") String name);
}
