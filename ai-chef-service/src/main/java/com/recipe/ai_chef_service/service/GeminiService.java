package com.recipe.ai_chef_service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.recipe.ai_chef_service.dto.IngredientDTO;
import com.recipe.ai_chef_service.dto.PantryItemDTO;
import com.recipe.ai_chef_service.dto.RecipeGenerationRequest;
import com.recipe.ai_chef_service.dto.RecipeSuggestion;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
    private final RestTemplate restTemplate;
    private final Random random = new Random();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.0-flash-exp}")
    private String model;

    @Value("${gemini.recipe.count:1}")
    private int recipeCount;

    public GeminiService() {
    this.restTemplate = new RestTemplate();
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5000);    // 5 seconds to connect
    factory.setReadTimeout(10000);      // 10 seconds for response
    // For connection pool timeout, you need to use HttpClient (advanced)
    // But for now, these two are enough
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

    int maxRetries = 2;  // Reduced from 3
    int retryCount = 0;
    long baseWaitTime = 1000;
    long startTime = System.currentTimeMillis();
    long maxTotalTime = 8000; // 8 seconds total timeout

    while (retryCount <= maxRetries) {
        // Check overall timeout
        if (System.currentTimeMillis() - startTime > maxTotalTime) {
            log.warn("⚠️ Overall timeout reached after {}ms", maxTotalTime);
            break;
        }

        try {
            String prompt = buildRecipeGenerationPrompt(pantryItems, preferences);
            String url = "https://generativelanguage.googleapis.com/v1/models/" + model + ":generateContent?key=" + apiKey;
            
            Map<String, Object> requestBody = buildGeminiRequest(prompt);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            if (retryCount > 0) {
                log.info("🔄 Retry attempt {}/{}", retryCount, maxRetries);
            }
            
            // Make the API call
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String aiResponse = extractTextFromResponse(response.getBody());
                return parseRecipeFromResponse(aiResponse, pantryItems, preferences);
            } else {
                log.error("Gemini API returned error: {}", response.getBody());
                break;
            }

        } catch (ResourceAccessException e) {
            // This catches socket timeouts, connection timeouts
            retryCount++;
            log.warn("⏱️ Network timeout/error, retry {}/{}: {}", retryCount, maxRetries, e.getMessage());
            
            if (retryCount > maxRetries) {
                break;
            }
            
            try {
                Thread.sleep(baseWaitTime * retryCount);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            
        } catch (HttpServerErrorException.ServiceUnavailable e) {
            // 503 handling
            retryCount++;
            log.warn("⚠️ API overloaded (503), retry {}/{}", retryCount, maxRetries);
            
            if (retryCount > maxRetries) {
                break;
            }
            
            try {
                Thread.sleep(baseWaitTime * (long) Math.pow(2, retryCount - 1));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            
        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            break;
        }
    }
    
    log.info("Using fallback recipes");
    return new ArrayList<>();
}   private Map<String, Object> buildGeminiRequest(String prompt) {
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
        generationConfig.put("temperature", 0.9); // Increased for more creativity
        generationConfig.put("topP", 0.95); // Added for more variety
        generationConfig.put("maxOutputTokens", 2048); // Increased for more detail
        generationConfig.put("topK", 40); // Added for more random selection
        
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
        prompt.append("You are a creative chef who invents unique and exciting recipe names. ");
        prompt.append("Create ").append(recipeCount).append(" delicious recipe(s) using primarily the available ingredients below.\n\n");
        
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
        
        prompt.append("\nCREATIVITY REQUIREMENTS FOR RECIPE NAMES:\n");
        prompt.append("- Recipe names MUST be UNIQUE, DESCRIPTIVE, and CREATIVE\n");
        prompt.append("- Include adjectives, cooking methods, or cultural references\n");
        prompt.append("- Make each recipe name different based on specific ingredients\n");
        prompt.append("- Avoid generic names like 'Stir Fry', 'Soup', 'Pasta Dish'\n");
        prompt.append("- GOOD examples: 'Spicy Ginger-Garlic Chicken Stir Fry', 'Creamy Tomato Basil Pasta', 'Hearty Autumn Vegetable Stew'\n");
        prompt.append("- BAD examples: 'Vegetable Stir Fry', 'Pasta', 'Soup'\n\n");
        
        prompt.append("IMPORTANT: You may suggest common pantry items (salt, pepper, oil, etc.) ");
        prompt.append("even if not listed above, but focus on using the listed ingredients.\n\n");
        
        prompt.append("RESPONSE FORMAT - Reply EXACTLY in this format:\n");
        
        for (int i = 1; i <= recipeCount; i++) {
            prompt.append("===RECIPE ").append(i).append(" START===\n");
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
            prompt.append("===RECIPE ").append(i).append(" END===\n");
            if (i < recipeCount) {
                prompt.append("\n");
            }
        }
        
        prompt.append("\nMake the recipes practical, delicious, and easy to follow! ");
        prompt.append("Each recipe should be distinctly different from the others.\n");
        
        log.debug("Generated prompt length: {}", prompt.length());
        return prompt.toString();
    }

    private List<RecipeSuggestion> parseRecipeFromResponse(String aiResponse, List<PantryItemDTO> pantryItems, RecipeGenerationRequest preferences) {
        List<RecipeSuggestion> recipes = new ArrayList<>();
        
        try {
            log.debug("Parsing AI response for {} recipes...", recipeCount);
            
            // Check if we have the expected format
            if (!aiResponse.contains("===RECIPE 1 START===")) {
                log.warn("AI response doesn't follow expected format, creating fallback recipe");
                return createFallbackRecipes(aiResponse, pantryItems, preferences);
            }
            
            // Parse multiple recipes
            for (int i = 1; i <= recipeCount; i++) {
                String startMarker = "===RECIPE " + i + " START===";
                String endMarker = "===RECIPE " + i + " END===";
                
                if (aiResponse.contains(startMarker) && aiResponse.contains(endMarker)) {
                    String recipeContent = aiResponse.substring(
                        aiResponse.indexOf(startMarker) + startMarker.length(),
                        aiResponse.indexOf(endMarker)
                    ).trim();
                    
                    RecipeSuggestion recipe = parseSingleRecipe(recipeContent, pantryItems, preferences);
                    if (recipe != null) {
                        recipes.add(recipe);
                        log.info("✅ Successfully created recipe {}: {}", i, recipe.getTitle());
                    }
                } else {
                    log.warn("Recipe {} not found in response, skipping", i);
                }
            }
            
            if (recipes.isEmpty()) {
                log.warn("No recipes parsed, creating fallback recipes");
                recipes.addAll(createFallbackRecipes(aiResponse, pantryItems, preferences));
            }
            
        } catch (Exception e) {
            log.error("❌ Error parsing recipe from AI response: {}", e.getMessage());
            e.printStackTrace();
            recipes.addAll(createFallbackRecipes(aiResponse, pantryItems, preferences));
        }
        
        return recipes;
    }
    
    private RecipeSuggestion parseSingleRecipe(String recipeContent, List<PantryItemDTO> pantryItems, RecipeGenerationRequest preferences) {
        try {
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
            
            // If recipe name is generic, enhance it
            String finalRecipeName = enhanceRecipeName(recipeName.trim(), pantryItems);
            
            return new RecipeSuggestion(
                finalRecipeName.isEmpty() ? generateCreativeRecipeName(pantryItems) : finalRecipeName,
                description.isEmpty() ? "Delicious recipe based on your ingredients" : description.trim(),
                ingredients,
                instructions,
                finalTotalTime > 0 ? finalTotalTime : 30, 
                servings > 0 ? servings : (preferences.getServings() != null ? preferences.getServings() : 2),
                difficulty.isEmpty() ? "Easy" : difficulty.trim(),
                cuisine.isEmpty() ? "International" : cuisine.trim(),
                0.95, 
                Arrays.asList("salt", "pepper", "oil", "water")
            );
            
        } catch (Exception e) {
            log.error("Error parsing single recipe: {}", e.getMessage());
            return null;
        }
    }
    
    private String enhanceRecipeName(String recipeName, List<PantryItemDTO> pantryItems) {
        if (recipeName == null || recipeName.isEmpty()) {
            return generateCreativeRecipeName(pantryItems);
        }
        
        // Check if name is too generic
        String lowerName = recipeName.toLowerCase();
        if (lowerName.contains("stir fry") || lowerName.contains("stir-fry")) {
            return addAdjectivesToRecipe(recipeName, pantryItems);
        } else if (lowerName.contains("pasta") || lowerName.contains("noodle")) {
            return addSauceDescription(recipeName, pantryItems);
        } else if (lowerName.contains("soup") || lowerName.contains("stew")) {
            return addHeartinessDescription(recipeName, pantryItems);
        }
        
        return recipeName;
    }
    
    private String addAdjectivesToRecipe(String baseName, List<PantryItemDTO> pantryItems) {
        String[] adjectives = {"Savory", "Spicy", "Creamy", "Crispy", "Zesty", "Herbed", "Garlicky", "Ginger"};
        String[] cookingStyles = {"Sautéed", "Pan-Fried", "Wok-Tossed", "Quick", "Easy", "Gourmet", "Sizzling"};
        String[] extras = {"Delight", "Fusion", "Medley", "Bowl", "Creation", "Special", "Feast"};
        
        String adjective = adjectives[random.nextInt(adjectives.length)];
        String style = cookingStyles[random.nextInt(cookingStyles.length)];
        String extra = extras[random.nextInt(extras.length)];
        
        // Get main ingredients for more specificity
        String mainProtein = pantryItems.stream()
            .filter(item -> item.getName().toLowerCase().contains("chicken") || 
                           item.getName().toLowerCase().contains("beef") ||
                           item.getName().toLowerCase().contains("tofu") ||
                           item.getName().toLowerCase().contains("shrimp"))
            .findFirst()
            .map(PantryItemDTO::getName)
            .orElse("");
        
        if (!mainProtein.isEmpty()) {
            return String.format("%s %s %s %s", adjective, style, mainProtein, extra);
        }
        
        return String.format("%s %s %s %s", adjective, style, baseName, extra);
    }
    
    private String addSauceDescription(String baseName, List<PantryItemDTO> pantryItems) {
        String[] sauceTypes = {"Creamy", "Tomato-Based", "Garlic", "Pesto", "Alfredo", "Marinara", "Arrabbiata"};
        String[] pastaTypes = {"Pasta", "Noodles", "Spaghetti", "Fettuccine", "Penne", "Fusilli"};
        String[] descriptors = {"Delight", "Toss", "Dish", "Perfection", "Special", "Creation"};
        
        String sauce = sauceTypes[random.nextInt(sauceTypes.length)];
        String pasta = pastaTypes[random.nextInt(pastaTypes.length)];
        String descriptor = descriptors[random.nextInt(descriptors.length)];
        
        return String.format("%s %s %s", sauce, pasta, descriptor);
    }
    
    private String addHeartinessDescription(String baseName, List<PantryItemDTO> pantryItems) {
        String[] heartiness = {"Hearty", "Comforting", "Nourishing", "Warming", "Rustic", "Homestyle"};
        String[] seasons = {"Autumn", "Winter", "Spring", "Summer", "Seasonal", "Farmhouse"};
        String[] types = {"Stew", "Soup", "Chowder", "Broth", "Potage", "Bisque"};
        
        String heart = heartiness[random.nextInt(heartiness.length)];
        String season = seasons[random.nextInt(seasons.length)];
        String type = types[random.nextInt(types.length)];
        
        // Get vegetable for specificity
        String mainVeg = pantryItems.stream()
            .filter(item -> item.getName().toLowerCase().contains("vegetable") ||
                           item.getName().toLowerCase().contains("carrot") ||
                           item.getName().toLowerCase().contains("potato") ||
                           item.getName().toLowerCase().contains("tomato"))
            .findFirst()
            .map(PantryItemDTO::getName)
            .orElse("Vegetable");
        
        return String.format("%s %s %s %s", heart, season, mainVeg, type);
    }
    
    private String generateCreativeRecipeName(List<PantryItemDTO> pantryItems) {
        // Get top 2-3 ingredients for the name
        List<String> mainIngredients = pantryItems.stream()
            .limit(3)
            .map(PantryItemDTO::getName)
            .collect(Collectors.toList());
        
        String[] recipeTypes = {"Fusion Bowl", "Kitchen Creation", "Pantry Special", "Chef's Choice", "Quick Fix"};
        String[] cookingMethods = {"Roasted", "Grilled", "Baked", "Sautéed", "Steamed", "Simmered"};
        
        String method = cookingMethods[random.nextInt(cookingMethods.length)];
        String type = recipeTypes[random.nextInt(recipeTypes.length)];
        
        if (!mainIngredients.isEmpty()) {
            String ingredientsStr = String.join(" & ", mainIngredients);
            return String.format("%s %s %s", method, ingredientsStr, type);
        }
        
        return String.format("Creative %s %s", method, type);
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
    
    private List<RecipeSuggestion> createFallbackRecipes(String aiResponse, List<PantryItemDTO> pantryItems, RecipeGenerationRequest preferences) {
        List<RecipeSuggestion> recipes = new ArrayList<>();
        
        // Create 2-3 fallback recipes with varied names
        int numRecipes = Math.min(3, Math.max(1, recipeCount));
        
        for (int i = 0; i < numRecipes; i++) {
            String recipeName = generateCreativeRecipeName(pantryItems);
            String description = aiResponse.length() > 100 ? 
                aiResponse.substring(0, Math.min(100, aiResponse.length())) + "..." : 
                "Delicious recipe based on your available ingredients";
            
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
                "1. Prepare all ingredients as needed",
                "2. Follow the cooking instructions for your chosen recipe",
                "3. Adjust seasoning to taste",
                "4. Plate and garnish if desired",
                "5. Serve and enjoy your creation!"
            );
            
            RecipeSuggestion recipe = new RecipeSuggestion(
                recipeName,
                description,
                ingredients,
                instructions,
                30,
                preferences.getServings() != null ? preferences.getServings() : 2,
                "Easy",
                "International",
                0.8,
                Arrays.asList("salt", "pepper", "oil", "herbs")
            );
            
            recipes.add(recipe);
        }
        
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