package com.recipe.ai_chef_service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.recipe.ai_chef_service.dto.IngredientDTO;
import com.recipe.ai_chef_service.dto.PantryItemDTO;
import com.recipe.ai_chef_service.dto.RecipeGenerationRequest;
import com.recipe.ai_chef_service.dto.RecipeSuggestion;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-001}")
    private String model;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        restTemplate.setRequestFactory(factory);
    }

    public String getApiKey() {
        return apiKey;
    }

    public List<RecipeSuggestion> generateRecipesWithGemini(List<PantryItemDTO> pantryItems,
            RecipeGenerationRequest preferences) {

        log.info("🔍 GeminiService called with model: {}", model);

        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("your-")) {
            log.warn("⚠️ Gemini API key not properly configured");
            return new ArrayList<>();
        }

        try {
            String prompt = buildRecipeGenerationPrompt(pantryItems, preferences);
            String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + apiKey;
            
            Map<String, Object> requestBody = buildGeminiRequest(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);


            if (response.getStatusCode() == HttpStatus.OK) {
                String aiResponse = extractTextFromResponse(response.getBody());
                return parseRecipeFromResponse(aiResponse, pantryItems);
            } else {
                log.error("Gemini API returned error: {}", response.getBody());
            }

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private Map<String, Object> buildGeminiRequest(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(part);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(content);
        requestBody.put("contents", contents);

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.7);
        generationConfig.put("maxOutputTokens", 1024);
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    private String extractTextFromResponse(Map<String, Object> responseBody) {
        try {
            log.debug("Extracting text from response: {}", responseBody.keySet());
            
            if (responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    if (firstCandidate.containsKey("content")) {
                        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                        if (content.containsKey("parts")) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                Map<String, Object> firstPart = parts.get(0);
                                if (firstPart.containsKey("text")) {
                                    return (String) firstPart.get("text");
                                }
                            }
                        }
                    }
                }
            }

            if (responseBody.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseBody.get("error");
                log.error("❌ Gemini API error in response: {}", error.get("message"));
            }

        } catch (Exception e) {
            log.error("❌ Failed to extract text from Gemini response: {}", e.getMessage());
            log.debug("Full response: {}", responseBody);
        }

        return "";
    }

    private String buildRecipeGenerationPrompt(List<PantryItemDTO> pantryItems,
            RecipeGenerationRequest preferences) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional chef and recipe generator. Create ONE delicious recipe using primarily the available ingredients below.\n\n");
        
        prompt.append("AVAILABLE INGREDIENTS:\n");
        for (PantryItemDTO item : pantryItems) {
            prompt.append("- ").append(item.getName())
                  .append(" (").append(item.getQuantity()).append(" ").append(item.getUnit()).append(")\n");
        }
        
        prompt.append("\nUSER PREFERENCES:\n");
        if (preferences.getMealType() != null && !preferences.getMealType().equals("any")) {
            prompt.append("- Meal type: ").append(preferences.getMealType()).append("\n");
        }
        if (preferences.getMaxPreparationTime() != null) {
            prompt.append("- Maximum prep time: ").append(preferences.getMaxPreparationTime()).append(" minutes\n");
        }
        if (preferences.getServings() != null) {
            prompt.append("- Servings: ").append(preferences.getServings()).append("\n");
        }
        if (preferences.getDifficulty() != null && !preferences.getDifficulty().equals("any")) {
            prompt.append("- Difficulty level: ").append(preferences.getDifficulty()).append("\n");
        }
        
        prompt.append("\nIMPORTANT: You may suggest common pantry items (salt, pepper, oil, etc.) ");
        prompt.append("even if not listed above, but focus on using the listed ingredients.\n\n");
        
        prompt.append("RESPONSE FORMAT - Reply EXACTLY in this format:\n");
        prompt.append("===RECIPE START===\n");
        prompt.append("Recipe Name: [Creative recipe name]\n");
        prompt.append("Description: [Brief description (1-2 sentences)]\n");
        prompt.append("Prep Time: [number] minutes\n");
        prompt.append("Cook Time: [number] minutes\n");
        prompt.append("Total Time: [number] minutes\n");
        prompt.append("Servings: [number]\n");
        prompt.append("Difficulty: [Easy/Medium/Hard]\n");
        prompt.append("Cuisine: [Type of cuisine]\n");
        prompt.append("\nINGREDIENTS:\n");
        prompt.append("- [Quantity] [Unit] [Ingredient name]\n");
        prompt.append("- [Quantity] [Unit] [Ingredient name]\n");
        prompt.append("\nINSTRUCTIONS:\n");
        prompt.append("1. [Step 1]\n");
        prompt.append("2. [Step 2]\n");
        prompt.append("3. [Step 3]\n");
        prompt.append("===RECIPE END===\n");
        
        prompt.append("\nMake the recipe practical, delicious, and easy to follow!");
        
        log.debug("Generated prompt length: {}", prompt.length());
        return prompt.toString();
    }

    private List<RecipeSuggestion> parseRecipeFromResponse(String aiResponse, List<PantryItemDTO> pantryItems) {
        List<RecipeSuggestion> recipes = new ArrayList<>();
        
        try {
            log.debug("Parsing AI response for recipe...");
            
            if (!aiResponse.contains("===RECIPE START===") || !aiResponse.contains("===RECIPE END===")) {
                log.warn("AI response doesn't follow expected format, creating fallback recipe");
                return createFallbackRecipe(aiResponse, pantryItems);
            }
            
            String recipeContent = aiResponse.substring(
                aiResponse.indexOf("===RECIPE START===") + 18,
                aiResponse.indexOf("===RECIPE END===")
            ).trim();
            
            log.debug("Extracted recipe content length: {}", recipeContent.length());
            
            String recipeName = extractValue(recipeContent, "Recipe Name:");
            String description = extractValue(recipeContent, "Description:");
            String prepTimeStr = extractValue(recipeContent, "Prep Time:").replaceAll("[^0-9]", "");
            String cookTimeStr = extractValue(recipeContent, "Cook Time:").replaceAll("[^0-9]", "");
            String totalTimeStr = extractValue(recipeContent, "Total Time:").replaceAll("[^0-9]", "");
            String servingsStr = extractValue(recipeContent, "Servings:").replaceAll("[^0-9]", "");
            String difficulty = extractValue(recipeContent, "Difficulty:");
            String cuisine = extractValue(recipeContent, "Cuisine:");
            
            List<IngredientDTO> ingredients = parseIngredientsFromText(recipeContent, pantryItems);
            
            List<String> instructions = parseInstructionsFromText(recipeContent);
            
            int prepTime = safeParseInt(prepTimeStr);
            int cookTime = safeParseInt(cookTimeStr);
            int totalTime = safeParseInt(totalTimeStr);
            int servings = safeParseInt(servingsStr);
            
            int finalTotalTime = totalTime > 0 ? totalTime : (prepTime + cookTime);
            
            RecipeSuggestion recipe = new RecipeSuggestion(
                recipeName.isEmpty() ? "AI Generated Recipe" : recipeName.trim(),
                description.isEmpty() ? "Delicious recipe based on your ingredients" : description.trim(),
                ingredients,
                instructions,
                finalTotalTime > 0 ? finalTotalTime : 30, 
                servings > 0 ? servings : 2,
                difficulty.isEmpty() ? "Easy" : difficulty.trim(),
                cuisine.isEmpty() ? "Various" : cuisine.trim(),
                0.95, 
                Arrays.asList("salt", "pepper", "oil", "water")
            );
            
            recipes.add(recipe);
            log.info("✅ Successfully created recipe: {}", recipeName);
            
        } catch (Exception e) {
            log.error("❌ Error parsing recipe from AI response: {}", e.getMessage());
            e.printStackTrace();
            recipes.addAll(createFallbackRecipe(aiResponse, pantryItems));
        }
        
        return recipes;
    }
    
    private String extractValue(String text, String fieldName) {
        try {
            if (!text.contains(fieldName)) {
                return "";
            }
            
            int start = text.indexOf(fieldName) + fieldName.length();
            int end = text.indexOf("\n", start);
            if (end == -1) {
                end = text.length();
            }
            
            return text.substring(start, end).trim();
        } catch (Exception e) {
            log.warn("Error extracting field {}: {}", fieldName, e.getMessage());
            return "";
        }
    }
    
    private List<IngredientDTO> parseIngredientsFromText(String recipeContent, List<PantryItemDTO> pantryItems) {
        List<IngredientDTO> ingredients = new ArrayList<>();
        
        try {
            if (recipeContent.contains("INGREDIENTS:") && recipeContent.contains("INSTRUCTIONS:")) {
                int start = recipeContent.indexOf("INGREDIENTS:") + 12;
                int end = recipeContent.indexOf("INSTRUCTIONS:");
                String ingredientsText = recipeContent.substring(start, end).trim();
                
                String[] lines = ingredientsText.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.startsWith("-") && line.length() > 1) {
                        String ingredientLine = line.substring(1).trim();
                        IngredientDTO dto = parseIngredientLine(ingredientLine);
                        if (dto != null) {
                            ingredients.add(dto);
                        }
                    }
                }
            }
            
            if (ingredients.size() < 5 && pantryItems != null) {
                List<IngredientDTO> pantryIngredients = pantryItems.stream()
                    .limit(3)
                    .map(item -> {
                        IngredientDTO dto = new IngredientDTO();
                        dto.setName(item.getName());
                        dto.setQuantity(Math.min(item.getQuantity(), 200));
                        dto.setUnit(item.getUnit());
                        return dto;
                    })
                    .collect(Collectors.toList());
                
                ingredients.addAll(pantryIngredients);
            }
            
        } catch (Exception e) {
            log.warn("Error parsing ingredients: {}", e.getMessage());
        }
        
        if (ingredients.isEmpty()) {
            ingredients.add(new IngredientDTO("Your ingredients", 1.0, "portion"));
        }
        
        return ingredients;
    }
    
    private IngredientDTO parseIngredientLine(String line) {
        try {
            IngredientDTO dto = new IngredientDTO();
            
            String[] parts = line.split("\\s+", 3);
            if (parts.length >= 3) {
                try {
                    dto.setQuantity(Double.parseDouble(parts[0]));
                    dto.setUnit(parts[1]);
                    dto.setName(parts[2]);
                } catch (NumberFormatException e) {
                    dto.setName(line);
                    dto.setQuantity(1.0);
                    dto.setUnit("portion");
                }
            } else {
                dto.setName(line);
                dto.setQuantity(1.0);
                dto.setUnit("portion");
            }
            
            return dto;
        } catch (Exception e) {
            log.warn("Could not parse ingredient line: {}", line);
            return null;
        }
    }
    
    private List<String> parseInstructionsFromText(String recipeContent) {
        List<String> instructions = new ArrayList<>();
        
        try {
            if (recipeContent.contains("INSTRUCTIONS:")) {
                int start = recipeContent.indexOf("INSTRUCTIONS:") + 13;
                String instructionsText = recipeContent.substring(start).trim();
                
                String[] lines = instructionsText.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.matches("^\\d+\\..*") || line.matches("^\\d+\\).*")) {
                        String instruction = line.replaceFirst("^\\d+[\\.\\)]\\s*", "");
                        if (!instruction.isEmpty()) {
                            instructions.add(instruction);
                        }
                    } else if (!line.isEmpty() && instructions.size() > 0) {
                        String lastInstruction = instructions.get(instructions.size() - 1);
                        instructions.set(instructions.size() - 1, lastInstruction + " " + line);
                    }
                }
            }
            
            if (instructions.isEmpty()) {
                instructions.add("Follow the recipe instructions above");
                instructions.add("Adjust seasoning to taste");
                instructions.add("Serve and enjoy");
            }
            
        } catch (Exception e) {
            log.warn("Error parsing instructions: {}", e.getMessage());
            instructions.add("Follow the AI recipe instructions");
        }
        
        return instructions;
    }
    
    private List<RecipeSuggestion> createFallbackRecipe(String aiResponse, List<PantryItemDTO> pantryItems) {
        List<RecipeSuggestion> recipes = new ArrayList<>();
        
        String recipeName = "AI Suggested Recipe";
        String description = aiResponse.length() > 150 ? aiResponse.substring(0, 150) + "..." : aiResponse;
        
        List<IngredientDTO> ingredients = new ArrayList<>();
        if (pantryItems != null && !pantryItems.isEmpty()) {
            ingredients = pantryItems.stream()
                .limit(3)
                .map(item -> {
                    IngredientDTO dto = new IngredientDTO();
                    dto.setName(item.getName());
                    dto.setQuantity(Math.min(item.getQuantity(), 200));
                    dto.setUnit(item.getUnit());
                    return dto;
                })
                .collect(Collectors.toList());
        } else {
            ingredients.add(new IngredientDTO("Available ingredients", 1.0, "portion"));
        }
        
        List<String> instructions = Arrays.asList(
            "1. Follow the AI recipe suggestions above",
            "2. Prepare ingredients as directed",
            "3. Cook according to instructions",
            "4. Adjust seasoning to taste",
            "5. Serve and enjoy"
        );
        
        RecipeSuggestion recipe = new RecipeSuggestion(
            recipeName,
            description,
            ingredients,
            instructions,
            30,
            2,
            "Easy",
            "AI Generated",
            0.8,
            Arrays.asList("salt", "pepper", "oil")
        );
        
        recipes.add(recipe);
        return recipes;
    }
    
    private int safeParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}