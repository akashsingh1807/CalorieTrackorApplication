package com.calorie.tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "indian_food")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndianFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "calories", nullable = false, precision = 10, scale = 2)
    private BigDecimal calories;

    @Column(name = "protein", precision = 10, scale = 2)
    private BigDecimal protein;

    @Column(name = "fat", precision = 10, scale = 2)
    private BigDecimal fat;

    @Column(name = "carbohydrate", precision = 10, scale = 2)
    private BigDecimal carbohydrate;
}
