package com.calorie.tracker.service;

import com.calorie.tracker.dto.FoodItemDto;
import com.calorie.tracker.model.AiRequest;
import com.calorie.tracker.model.FoodAnalysisCache;
import com.calorie.tracker.repository.AiRequestRepository;
import com.calorie.tracker.repository.FoodAnalysisCacheRepository;
import com.calorie.tracker.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GeminiVisionService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiVisionService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private AiRequestRepository aiRequestRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodAnalysisCacheRepository foodAnalysisCacheRepository;

    public GeminiVisionService() {
        this.webClient = WebClient.builder()
                .exchangeStrategies(org.springframework.web.reactive.function.client.ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private void logAiRequest(Long userId, String requestType, int estimatedTokens) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                AiRequest request = AiRequest.builder()
                        .user(user)
                        .requestType(requestType)
                        .tokensUsed(estimatedTokens)
                        .createdAt(LocalDateTime.now())
                        .build();
                aiRequestRepository.save(request);
            });
        }
    }

    private List<FoodItemDto> callGeminiApi(String requestBody) {
        logger.info("Calling Gemini API. URL: {}, API Key length: {}", apiUrl, apiKey != null ? apiKey.length() : 0);
        try {
            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(response);
            
            // Handle API errors in response body
            if (rootNode.has("error")) {
                logger.error("Gemini API returned error: {}", rootNode.path("error").path("message").asText());
                return new ArrayList<>();
            }

            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String textResponse = parts.get(0).path("text").asText();
                    
                    // Clean up markdown block if present
                    textResponse = textResponse.replaceAll("```json", "").replaceAll("```", "").trim();
                    
                    try {
                        // Parse the JSON array of objects into List<FoodItemDto>
                        return objectMapper.readValue(textResponse, new TypeReference<List<FoodItemDto>>() {});
                    } catch (Exception e) {
                        logger.warn("Failed to parse Gemini response as JSON array of objects, trying as strings: {}", e.getMessage());
                        // Fallback: If it's just a list of strings, convert to DTOs
                        List<String> foodNames = objectMapper.readValue(textResponse, new TypeReference<List<String>>() {});
                        List<FoodItemDto> items = new ArrayList<>();
                        for (String name : foodNames) {
                            FoodItemDto dto = new FoodItemDto();
                            dto.setName(name);
                            dto.setCalories(0.0); // Will be filled by NutritionService if needed
                            items.add(dto);
                        }
                        return items;
                    }
                }
            }
        } catch (WebClientResponseException e) {
            logger.error("WebClientResponseException calling Gemini API: Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error calling or parsing Gemini API: {}", e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public List<FoodItemDto> identifyFoodFromImage(Long userId, String imageUrl, String persona) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            logger.warn("identifyFoodFromImage called with empty imageUrl");
            return new ArrayList<>();
        }

        logAiRequest(userId, "IMAGE_DETECTION", 1000);
        
        String personaInstructions = "";
        if ("STRICT_TRAINER".equalsIgnoreCase(persona)) {
            personaInstructions = "\nAct as a strict, no-nonsense fitness trainer. If they log junk food, call them out on it.";
        } else if ("INDIAN_MOM".equalsIgnoreCase(persona)) {
            personaInstructions = "\nAct as a caring, slightly dramatic Indian Mom. If they log junk food, scold them lovingly.";
        }
        
        String promptText = "CRITICAL: You are an expert clinical dietician." + personaInstructions + " Analyze this image and identify all food items. " +
                "For each item, provide STRICTLY ACCURATE nutritional values matching the USDA FoodData Central database for the EXACT serving size. " +
                "DO NOT HALLUCINATE CALORIES. Vegetables like cucumbers, tomatoes, and leafy greens are VERY LOW in calories (~15-20 kcal per 100g). " +
                "Meats, oils, and grains are higher. Cross-check your estimates against standard verified food databases before outputting. " +
                "Return ONLY a JSON array of objects with these exact keys: " +
                "'name', 'servingSize', 'calories', 'protein', 'carbs', 'fat', " +
                "'fiber', 'sugar', 'sodium', 'potassium', 'calcium', 'iron', 'vitaminC', 'vitaminD'. " +
                "All numeric values must be highly accurate numbers (not zero or null). " +
                "Units: calories=kcal, protein/carbs/fat/fiber/sugar=grams, sodium/potassium/calcium/iron/vitaminC=mg, vitaminD=micrograms. " +
                "Return NOTHING else but the raw JSON array.";

        try {
            String base64Image = "";
            String mimeType = "image/jpeg";

            if (imageUrl.startsWith("data:") && imageUrl.contains(";base64,")) {
                mimeType = imageUrl.substring(imageUrl.indexOf(":") + 1, imageUrl.indexOf(";"));
                base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
            } else if (imageUrl.startsWith("http")) {
                logger.info("Downloading image for AI analysis: {}", imageUrl);
                byte[] imageBytes = webClient.get()
                        .uri(java.net.URI.create(imageUrl))
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();
                
                if (imageBytes != null) {
                    base64Image = Base64.getEncoder().encodeToString(imageBytes);
                    if (imageUrl.toLowerCase().contains(".png")) mimeType = "image/png";
                    else if (imageUrl.toLowerCase().contains(".webp")) mimeType = "image/webp";
                }
            } else {
                // Try treating as raw base64
                base64Image = imageUrl.contains(",") ? imageUrl.substring(imageUrl.indexOf(",") + 1) : imageUrl;
            }

            if (base64Image.isEmpty()) {
                logger.error("Failed to extract image data from: {}", imageUrl);
                return new ArrayList<>();
            }

            var textPart = Map.of("text", promptText);
            var imagePart = Map.of(
                "inlineData", Map.of(
                    "mimeType", mimeType,
                    "data", base64Image
                )
            );
            
            var contents = List.of(Map.of("parts", List.of(textPart, imagePart)));
            String requestBody = objectMapper.writeValueAsString(Map.of("contents", contents));

            return callGeminiApi(requestBody);
            
        } catch (Exception e) {
            logger.error("Gemini Image API preparation failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<FoodItemDto> analyzeText(Long userId, String text, String persona) {
        if (text == null || text.trim().isEmpty()) return new ArrayList<>();

        String normalizedQuery = text.trim().toLowerCase();
        String personaStr = (persona != null ? persona.trim().toUpperCase() : "NONE");
        String cacheKey = personaStr + ":" + normalizedQuery;

        try {
            var cached = foodAnalysisCacheRepository.findByQueryKey(cacheKey);
            if (cached.isPresent()) {
                logger.info("Cache hit for key: {}. Returning cached food analysis.", cacheKey);
                return objectMapper.readValue(cached.get().getResponseJson(), new TypeReference<List<FoodItemDto>>() {});
            }
        } catch (Exception e) {
            logger.error("Error reading from food analysis cache for key {}: {}", cacheKey, e.getMessage(), e);
        }

        logAiRequest(userId, "TEXT_ANALYSIS", text.length() / 2);
        
        String personaInstructions = "";
        if ("STRICT_TRAINER".equalsIgnoreCase(persona)) {
            personaInstructions = "\nAct as a strict, no-nonsense fitness trainer. If they log junk food, call them out on it.";
        } else if ("INDIAN_MOM".equalsIgnoreCase(persona)) {
            personaInstructions = "\nAct as a caring, slightly dramatic Indian Mom. If they log junk food, scold them lovingly (e.g. 'Arre beta, why are you eating outside?').";
        }
        
        String promptText = "CRITICAL: You are an expert clinical dietician specializing in Indian and global cuisine." + personaInstructions + "\n" +
                "Analyze this food description: '" + text + "'\n\n" +
                "Instructions:\n" +
                "1. Extract EACH separate food item mentioned. Autocorrect any spelling mistakes, typos, phonetic spellings, or informal names automatically (e.g. interpret 'aalo paratha' or 'aloo pratha' as 'Aloo Paratha', 'chiken' as 'Chicken', etc.) before analyzing.\n" +
                "2. Use STRICTLY ACCURATE nutritional data matching the USDA FoodData Central database.\n" +
                "3. DO NOT HALLUCINATE CALORIES. Vegetables (like cucumbers, lettuce) are VERY LOW in calories (~15 kcal per medium cucumber). Only oils, grains, meats, and sweets are high calorie.\n" +
                "4. If quantities are given (e.g., '200g', '2 eggs', '1 cup'), use those EXACT quantities to calculate nutrition.\n" +
                "5. If no quantity given, assume a typical single serving (e.g. 1 medium cucumber = 300g = 45 kcal).\n" +
                "6. Provide ACCURATE nutrition for the EXACT quantity specified — NOT per 100g.\n" +
                "7. All numeric values must be scientifically accurate numbers based on verified food databases.\n\n" +
                "Return ONLY a JSON array of objects. Each object must have these EXACT keys:\n" +
                "- 'name': food name with quantity (e.g., 'Cooked Dal (200g)')\n" +
                "- 'servingSize': quantity string (e.g., '200g', '2 medium')\n" +
                "- 'calories': total kcal for this quantity (number)\n" +
                "- 'protein': grams of protein (number)\n" +
                "- 'carbs': grams of carbohydrates (number)\n" +
                "- 'fat': grams of fat (number)\n" +
                "- 'fiber': grams of dietary fiber (number)\n" +
                "- 'sugar': grams of sugar (number)\n" +
                "- 'sodium': milligrams of sodium (number)\n" +
                "- 'potassium': milligrams of potassium (number)\n" +
                "- 'calcium': milligrams of calcium (number)\n" +
                "- 'iron': milligrams of iron (number)\n" +
                "- 'vitaminC': milligrams of Vitamin C (number)\n" +
                "- 'vitaminD': micrograms of Vitamin D (number)\n\n" +
                "Example for '300g cooked dal':\n" +
                "[{\"name\":\"Cooked Dal (300g)\",\"servingSize\":\"300g\",\"calories\":345,\"protein\":21,\"carbs\":54,\"fat\":1.5,\"fiber\":9,\"sugar\":3,\"sodium\":30,\"potassium\":630,\"calcium\":60,\"iron\":5.4,\"vitaminC\":3,\"vitaminD\":0}]\n\n" +
                "Return NOTHING else but the raw JSON array. No markdown, no explanation.";

        try {
            var contents = List.of(Map.of("parts", List.of(Map.of("text", promptText))));
            String requestBody = objectMapper.writeValueAsString(Map.of("contents", contents));

            List<FoodItemDto> result = callGeminiApi(requestBody);
            
            if (result != null && !result.isEmpty()) {
                try {
                    String jsonResponse = objectMapper.writeValueAsString(result);
                    FoodAnalysisCache cacheEntry = FoodAnalysisCache.builder()
                            .queryKey(cacheKey)
                            .responseJson(jsonResponse)
                            .createdAt(LocalDateTime.now())
                            .build();
                    foodAnalysisCacheRepository.save(cacheEntry);
                    logger.info("Saved search result to cache for key: {}", cacheKey);
                } catch (Exception e) {
                    logger.error("Failed to write to food analysis cache for key {}: {}", cacheKey, e.getMessage(), e);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Gemini Text API preparation failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getMealSuggestions(Long userId, String goal) {
        logAiRequest(userId, "MEAL_SUGGESTION", 300);
        
        String promptText = "Provide 3 healthy meal suggestions for a user whose fitness goal is " + goal + ". " +
                "Return ONLY a JSON list of strings.";

        logger.info("Calling Gemini getMealSuggestions. URL: {}, API Key length: {}", apiUrl, apiKey != null ? apiKey.length() : 0);
        try {
            var contents = List.of(Map.of("parts", List.of(Map.of("text", promptText))));
            String requestBody = objectMapper.writeValueAsString(Map.of("contents", contents));

            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(response);
            String textResponse = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            textResponse = textResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            
            return objectMapper.readValue(textResponse, new TypeReference<List<String>>() {});
        } catch (WebClientResponseException e) {
            logger.error("WebClientResponseException in getMealSuggestions: Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of("Grilled Chicken Salad", "Oats with Berries", "Quinoa Bowl");
        } catch (Exception e) {
            logger.error("Gemini Suggestion API failed: {}", e.getMessage(), e);
            return List.of("Grilled Chicken Salad", "Oats with Berries", "Quinoa Bowl");
        }
    }
}
