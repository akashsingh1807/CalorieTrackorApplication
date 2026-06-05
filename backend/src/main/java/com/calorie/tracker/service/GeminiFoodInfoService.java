package com.calorie.tracker.service;

import com.calorie.tracker.dto.GeminiFoodResponseDto;
import com.calorie.tracker.model.FoodInfo;
import com.calorie.tracker.repository.FoodInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Optional;

@Service
public class GeminiFoodInfoService {

    RestTemplate restTemplate = new RestTemplate();
    private final String geminiApiKey;
    private final String geminiUrl;
    private final FoodInfoRepository foodInfoRepository;
    private final ObjectMapper objectMapper;

    public GeminiFoodInfoService(@Value("${gemini.api.key}") String apiKey,
                                 @Value("${gemini.api.url}") String apiUrl,
                                 FoodInfoRepository foodInfoRepository,
                                 ObjectMapper objectMapper) {
        this.geminiApiKey = apiKey;
        this.geminiUrl = apiUrl;
        this.foodInfoRepository = foodInfoRepository;
        this.objectMapper = objectMapper;
    }

    public FoodInfo fetchAndStore(String query) {
        try {
            // Check if it already exists in the database by name
            Optional<FoodInfo> existing = foodInfoRepository.findByNameIgnoreCase(query.trim().toLowerCase());
            if (existing.isPresent()) {
                return existing.get();
            }

            String systemInstructionText = "You are a precise, clinical nutritional analysis engine for the CalorieTrackorApplication. \n" +
                    "Your task is to analyze natural language food queries, calculate or estimate their macronutrient values, and output strictly formatted JSON.\n\n" +
                    "### Core Instructions:\n" +
                    "1. Identify the core food item(s), preparation method, and portion size from the user's input.\n" +
                    "2. If no explicit portion size is provided, assume a logical standard serving (e.g., \"100g\", \"1 medium piece\", \"1 standard katori/bowl\" for Indian dals/curries).\n" +
                    "3. For regional dishes (especially Indian cuisine), account for standard preparation methods (e.g., oil/ghee usage) when estimating calories and fats.\n" +
                    "4. If the user inputs a combined meal (e.g., \"2 rotis and a bowl of paneer butter masala\"), aggregate the total macros into a single combined output.\n\n" +
                    "### Data Normalization Rules:\n" +
                    "- foodName: Must be lowercase, concise, and stripped of subjective adjectives (e.g., \"delicious hot chicken biryani\" becomes \"chicken biryani\"). Retain words that affect macros (e.g., \"fried\", \"boiled\").\n" +
                    "- isFood: Boolean. Set to false if the user inputs gibberish, non-consumable items (e.g., \"a car tire\"), or harmful substances.\n" +
                    "- Numbers: All macronutrients must be strictly numeric (float or integer). Do not include units like \"g\" inside the numeric fields.\n" +
                    "- servingUnit: Use standard, predictable strings (e.g., \"g\", \"ml\", \"piece\", \"plate\", \"bowl\", \"slice\", \"cup\").\n\n" +
                    "### Execution Steps (Chain of Thought):\n" +
                    "You must always follow the schema order.\n" +
                    "First, use the \"reasoning\" field to briefly show your math. Break down the item, the assumed weight, and the macro estimation per ingredient. \n" +
                    "Second, output the final aggregated numbers in the respective JSON fields.\n\n" +
                    "### Constraints:\n" +
                    "- Output ONLY valid JSON.\n" +
                    "- Do not wrap the response in markdown blocks (e.g., no ```json).\n" +
                    "- Never include conversational filler outside the JSON object.";

            JSONObject requestBody = new JSONObject();
            
            JSONObject systemInstruction = new JSONObject();
            systemInstruction.put("parts", new JSONArray().put(new JSONObject().put("text", systemInstructionText)));
            requestBody.put("systemInstruction", systemInstruction);

            String userPromptText = String.format("Analyze the following food input: \n\"%s\"", query);
            requestBody.put("contents", new JSONArray().put(new JSONObject()
                    .put("role", "user")
                    .put("parts", new JSONArray().put(new JSONObject().put("text", userPromptText)))
            ));

            // JSON Schema
            JSONObject schema = new JSONObject();
            schema.put("type", "object");
            JSONObject properties = new JSONObject();
            properties.put("reasoning", new JSONObject().put("type", "string").put("description", "Step-by-step breakdown of the assumed ingredients, weights, and math used to calculate the final macros."));
            properties.put("isFood", new JSONObject().put("type", "boolean").put("description", "True if the input is an edible food item. False if it is gibberish or non-food."));
            properties.put("foodName", new JSONObject().put("type", "string").put("description", "The normalized, lowercase name of the food."));
            properties.put("calories", new JSONObject().put("type", "number").put("description", "Total kilocalories (kcal)."));
            properties.put("protein", new JSONObject().put("type", "number").put("description", "Total protein in grams."));
            properties.put("carbohydrates", new JSONObject().put("type", "number").put("description", "Total carbohydrates in grams."));
            properties.put("fat", new JSONObject().put("type", "number").put("description", "Total fat in grams."));
            properties.put("servingSize", new JSONObject().put("type", "number").put("description", "The numeric quantity of the serving."));
            properties.put("servingUnit", new JSONObject().put("type", "string").put("description", "The unit of measurement (e.g., g, piece, bowl, plate)."));
            schema.put("properties", properties);
            schema.put("required", new JSONArray().put("reasoning").put("isFood").put("foodName").put("calories").put("protein").put("carbohydrates").put("fat").put("servingSize").put("servingUnit"));

            JSONObject generationConfig = new JSONObject();
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("responseSchema", schema);
            
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            String url = geminiUrl;
            if (!url.contains("key=")) {
                headers.set("x-goog-api-key", geminiApiKey);
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            
            if (response.getStatusCode() != HttpStatus.OK) {
                return null;
            }

            JSONObject json = new JSONObject(response.getBody());
            String text = json.optJSONArray("candidates").optJSONObject(0).optJSONObject("content").optJSONArray("parts").optJSONObject(0).optString("text");

            GeminiFoodResponseDto dto = objectMapper.readValue(text.trim(), GeminiFoodResponseDto.class);

            if (dto.getIsFood() != null && dto.getIsFood()) {
                FoodInfo entity = new FoodInfo();
                entity.setSourceId("GEMINI_" + System.currentTimeMillis());
                entity.setName(dto.getFoodName() != null ? dto.getFoodName() : query);
                entity.setCalories(dto.getCalories());
                entity.setProtein(dto.getProtein());
                entity.setCarbs(dto.getCarbohydrates());
                entity.setFat(dto.getFat());
                entity.setServingSize(dto.getServingSize());
                entity.setServingUnit(dto.getServingUnit());
                entity.setRawJson(text.trim());
                
                return foodInfoRepository.save(entity);
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
