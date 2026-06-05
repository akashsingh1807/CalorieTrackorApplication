package com.calorie.tracker.service;

import com.calorie.tracker.dto.GeminiFoodResponseDto;
import com.calorie.tracker.model.FoodInfo;
import com.calorie.tracker.repository.FoodInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GeminiFoodInfoServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private FoodInfoRepository foodInfoRepository;

    private GeminiFoodInfoService geminiService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        geminiService = new GeminiFoodInfoService("fake-key", "https://api.fake.com", foodInfoRepository, objectMapper);
        geminiService.restTemplate = restTemplate;
    }

    @Test
    public void testFetchAndStore_whenCacheHit() {
        FoodInfo cachedInfo = new FoodInfo();
        cachedInfo.setName("apple");
        cachedInfo.setCalories(52.0);

        when(foodInfoRepository.findByNameIgnoreCase("apple")).thenReturn(Optional.of(cachedInfo));

        FoodInfo result = geminiService.fetchAndStore("apple");

        assertNotNull(result);
        assertEquals(52.0, result.getCalories());
        verifyNoInteractions(restTemplate);
    }

    @Test
    public void testFetchAndStore_whenCacheMiss_andValidGeminiResponse() {
        when(foodInfoRepository.findByNameIgnoreCase("banana")).thenReturn(Optional.empty());

        // Mocking the Gemini response JSON structure
        String mockResponseJson = "{" +
                "  \"candidates\": [" +
                "    {" +
                "      \"content\": {" +
                "        \"parts\": [" +
                "          {" +
                "            \"text\": \"{\\n  \\\"isFood\\\": true,\\n  \\\"foodName\\\": \\\"Banana\\\",\\n  \\\"servingSize\\\": 1.0,\\n  \\\"servingUnit\\\": \\\"medium\\\",\\n  \\\"calories\\\": 105.0,\\n  \\\"protein\\\": 1.3,\\n  \\\"fat\\\": 0.4,\\n  \\\"carbohydrates\\\": 27.0\\n}\"" +
                "          }" +
                "        ]" +
                "      }" +
                "    }" +
                "  ]" +
                "}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        when(foodInfoRepository.save(any(FoodInfo.class))).thenAnswer(i -> {
            FoodInfo info = i.getArgument(0);
            info.setId(1L);
            return info;
        });

        FoodInfo result = geminiService.fetchAndStore("banana");

        assertNotNull(result);
        assertEquals("Banana", result.getName());
        assertEquals(1, result.getServingSize());
        assertEquals("medium", result.getServingUnit());
        assertEquals(105.0, result.getCalories());
        assertEquals(1.3, result.getProtein());
        assertEquals(0.4, result.getFat());
        assertEquals(27.0, result.getCarbs());

        verify(foodInfoRepository).save(any(FoodInfo.class));
    }
    
    @Test
    public void testFetchAndStore_whenGeminiReturnsInvalidJson() {
        when(foodInfoRepository.findByNameIgnoreCase("invalid")).thenReturn(Optional.empty());

        String mockResponseJson = "{" +
                "  \"candidates\": [" +
                "    {" +
                "      \"content\": {" +
                "        \"parts\": [" +
                "          {" +
                "            \"text\": \"Sorry, I could not find information on that.\"" +
                "          }" +
                "        ]" +
                "      }" +
                "    }" +
                "  ]" +
                "}";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        FoodInfo result = geminiService.fetchAndStore("invalid");

        assertNull(result);
        verify(foodInfoRepository, never()).save(any(FoodInfo.class));
    }
}
