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
    public void testAnalyzeText_CacheHit() {
        String text = "aalo paratha";
        String persona = "NONE";
        String cacheKey = "NONE:aalo paratha";
        String responseJson = "[{\"name\":\"Aloo Paratha\",\"calories\":290.0,\"servingSize\":\"1 piece\"}]";

        FoodAnalysisCache cacheEntry = FoodAnalysisCache.builder()
                .queryKey(cacheKey)
                .responseJson(responseJson)
                .createdAt(LocalDateTime.now())
                .build();

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
}

