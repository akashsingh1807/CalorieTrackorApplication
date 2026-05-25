package com.calorie.tracker.service;

import com.calorie.tracker.dto.FoodItemDto;
import com.calorie.tracker.model.FoodAnalysisCache;
import com.calorie.tracker.repository.AiRequestRepository;
import com.calorie.tracker.repository.FoodAnalysisCacheRepository;
import com.calorie.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GeminiVisionServiceTest {

    @InjectMocks
    private GeminiVisionService serviceUnderTest;

    @Mock
    private FoodAnalysisCacheRepository foodAnalysisCacheRepository;

    @Mock
    private AiRequestRepository aiRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    public void contextLoads() {
        assertNotNull(serviceUnderTest);
    }

    @Test
    public void testAnalyzeText_ExactCacheHit() {
        String text = "aalo paratha";
        String persona = "NONE";
        String cacheKey = "NONE:exact:aalo paratha";
        String responseJson = "[{\"name\":\"Aloo Paratha\",\"calories\":290.0,\"servingSize\":\"1 piece\"}]";

        FoodAnalysisCache cacheEntry = FoodAnalysisCache.builder()
                .queryKey(cacheKey)
                .responseJson(responseJson)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock exact cache lookup
        when(foodAnalysisCacheRepository.findByQueryKey("NONE:scaled:aalo paratha:piece")).thenReturn(Optional.empty());
        when(foodAnalysisCacheRepository.findByQueryKey(cacheKey)).thenReturn(Optional.of(cacheEntry));

        List<FoodItemDto> result = serviceUnderTest.analyzeText(1L, text, persona);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Aloo Paratha", result.get(0).getName());
        assertEquals(290.0, result.get(0).getCalories());
        assertEquals("1 piece", result.get(0).getServingSize());

        verify(foodAnalysisCacheRepository, times(1)).findByQueryKey(cacheKey);
        verifyNoInteractions(aiRequestRepository);
    }

    @Test
    public void testAnalyzeText_ScaledCacheHit() {
        String text = "chicken 200g";
        String persona = "NONE";
        String scaledCacheKey = "NONE:scaled:chicken:g";
        
        // Scaled JSON represents 1g of chicken
        String baseResponseJson = "[{\"name\":\"Chicken\",\"calories\":2.15,\"servingSize\":\"1g\",\"protein\":0.27,\"carbs\":0.0,\"fat\":0.11}]";

        FoodAnalysisCache cacheEntry = FoodAnalysisCache.builder()
                .queryKey(scaledCacheKey)
                .responseJson(baseResponseJson)
                .createdAt(LocalDateTime.now())
                .build();

        // Mock scaled cache hit
        when(foodAnalysisCacheRepository.findByQueryKey(scaledCacheKey)).thenReturn(Optional.of(cacheEntry));

        List<FoodItemDto> result = serviceUnderTest.analyzeText(1L, text, persona);

        assertNotNull(result);
        assertEquals(1, result.size());
        
        FoodItemDto scaledItem = result.get(0);
        assertEquals("Chicken (200g)", scaledItem.getName());
        assertEquals("200g", scaledItem.getServingSize());
        assertEquals(430.0, scaledItem.getCalories()); // 2.15 * 200
        assertEquals(54.0, scaledItem.getProtein());   // 0.27 * 200
        assertEquals(0.0, scaledItem.getCarbs());
        assertEquals(22.0, scaledItem.getFat());      // 0.11 * 200

        verify(foodAnalysisCacheRepository, times(1)).findByQueryKey(scaledCacheKey);
        // Exact cache lookup should not be called since scaled hit was successful
        verify(foodAnalysisCacheRepository, never()).findByQueryKey("NONE:exact:chicken 200g");
        verifyNoInteractions(aiRequestRepository);
    }
}


