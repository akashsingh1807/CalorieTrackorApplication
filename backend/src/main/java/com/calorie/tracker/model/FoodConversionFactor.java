package com.calorie.tracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "food_conversion_factor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodConversionFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fdc_id")
    private String fdcId;

    @Column(nullable = false)
    private String name;

    @Column(name = "protein")
    private BigDecimal protein;

    @Column(name = "fat")
    private BigDecimal fat;

    @Column(name = "carbohydrate")
    private BigDecimal carbohydrate;

    @Column(name = "calories")
    private BigDecimal calories;
}
