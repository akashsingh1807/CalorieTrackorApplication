package com.calorie.tracker.repository;

import com.calorie.tracker.model.FoodAnalysisCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodAnalysisCacheRepository extends JpaRepository<FoodAnalysisCache, Long> {
    Optional<FoodAnalysisCache> findByQueryKey(String queryKey);
}
