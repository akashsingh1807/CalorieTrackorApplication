package com.calorie.tracker.service;

import com.calorie.tracker.model.FoodConversionFactor;
import com.calorie.tracker.repository.FoodConversionFactorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Collections;

@Service
public class GeminiFoodInfoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String geminiApiKey;
    private final String geminiUrl;
    private final FoodConversionFactorRepository repository;

    public GeminiFoodInfoService(@Value("${gemini.api.key}") String apiKey,
                                 @Value("${gemini.api.url}") String apiUrl,
                                 FoodConversionFactorRepository repository) {
        this.geminiApiKey = apiKey;
        this.geminiUrl = apiUrl;
        this.repository = repository;
    }

    /**
     * Calls Gemini to obtain macro‑nutrient info for the given food name.
     * The prompt asks for protein, fat, carbohydrate and calories per 100 g.
     * The response is expected to be a JSON object like:
     * {
     *   "name": "Apple",
     *   "protein": 0.3,
     *   "fat": 0.2,
     *   "carbohydrate": 14.0,
     *   "calories": 52
     * }
     */
    public FoodConversionFactor fetchAndStore(String query) {
        try {
            String prompt = String.format("Provide protein, fat, carbohydrate (grams) and total calories per 100g for food item: %s. Respond ONLY with a JSON object containing fields: name, protein, fat, carbohydrate, calories.", query);

            JSONObject requestBody = new JSONObject();
            requestBody.put("contents", Collections.singletonList(new JSONObject().put("role", "user").put("parts", Collections.singletonList(new JSONObject().put("text", prompt)))));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(geminiApiKey);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(geminiUrl, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                return null;
            }
            String body = response.getBody();
            // Very naive parsing – real implementation should handle the Gemini response format properly.
            JSONObject json = new JSONObject(body);
            // Assuming the first candidate text contains the JSON we asked for.
            String text = json.optJSONArray("candidates").optJSONObject(0).optJSONArray("content").optJSONObject(0).optString("text");
            JSONObject data = new JSONObject(text.trim());

            FoodConversionFactor entity = new FoodConversionFactor();
            entity.setFdcId(null);
            entity.setName(data.optString("name", query));
            entity.setProtein(parseDecimal(data.optString("protein")));
            entity.setFat(parseDecimal(data.optString("fat")));
            entity.setCarbohydrate(parseDecimal(data.optString("carbohydrate")));
            entity.setCalories(parseDecimal(data.optString("calories")));

            // Persist for future fast look‑ups
            return repository.save(entity);
        } catch (Exception e) {
            // Log silently; in a real app use a logger
            return null;
        }
    }

    private BigDecimal parseDecimal(String s) {
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
