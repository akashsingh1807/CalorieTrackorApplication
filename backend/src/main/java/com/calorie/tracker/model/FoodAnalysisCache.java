package com.calorie.tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_analysis_cache", indexes = {
    @Index(name = "idx_query_key", columnList = "query_key", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodAnalysisCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "query_key", nullable = false, length = 1000, unique = true)
    private String queryKey;

    @Column(name = "response_json", nullable = false, columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
