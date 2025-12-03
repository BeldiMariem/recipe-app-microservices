package com.recipe.ai_chef_service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.recipe.ai_chef_service.client.PantryServiceClient;
import com.recipe.ai_chef_service.dto.IngredientDTO;
import com.recipe.ai_chef_service.dto.PantryItemDTO;
import com.recipe.ai_chef_service.dto.RecipeGenerationRequest;
import com.recipe.ai_chef_service.dto.RecipeGenerationResponse;
import com.recipe.ai_chef_service.dto.RecipeSuggestion;

@Service
public class AIChefService {

    private static final Logger log = LoggerFactory.getLogger(AIChefService.class);

    private final GeminiService geminiService;
    private final PantryServiceClient pantryServiceClient;

    @Value("${recipe.generation.use-expiring-first:true}")
    private boolean useExpiringFirst;

    public AIChefService(GeminiService geminiService, PantryServiceClient pantryServiceClient) {
        this.geminiService = geminiService;
        this.pantryServiceClient = pantryServiceClient;
    }

    public RecipeGenerationResponse generateRecipes(RecipeGenerationRequest request) {
        List<PantryItemDTO> pantryItems = null;

        try {
            pantryItems = pantryServiceClient.getUserPantry(request.getUserId());

            if (pantryItems.isEmpty()) {
                return createEmptyPantryResponse(request);
            }

            if (useExpiringFirst) {
                List<PantryItemDTO> expiringItems = pantryServiceClient.getExpiringItems(request.getUserId());
                pantryItems = prioritizeExpiringItems(pantryItems, expiringItems);
            }

            List<RecipeSuggestion> suggestions = geminiService.generateRecipesWithGemini(pantryItems, request);

            if (suggestions == null || suggestions.isEmpty()) {
                log.info("Gemini returned empty, using fallback recipes");
                suggestions = generateFallbackRecipes(pantryItems, request);
            }

            return new RecipeGenerationResponse(
                    suggestions,
                    UUID.randomUUID().toString(),
                    System.currentTimeMillis()
            );

        } catch (Exception e) {
            log.error("Error generating recipes: {}", e.getMessage());

            List<PantryItemDTO> itemsForFallback = pantryItems;
            if (itemsForFallback == null) {
                try {
                    itemsForFallback = pantryServiceClient.getUserPantry(request.getUserId());
                } catch (Exception ex) {
                    log.error("Failed to fetch pantry items: {}", ex.getMessage());
                    itemsForFallback = Collections.emptyList();
                }
            }

            List<RecipeSuggestion> fallbackSuggestions = generateFallbackRecipes(itemsForFallback, request);

            return new RecipeGenerationResponse(
                    fallbackSuggestions,
                    "fallback-" + UUID.randomUUID().toString(),
                    System.currentTimeMillis()
            );
        }
    }

    private RecipeGenerationResponse createEmptyPantryResponse(RecipeGenerationRequest request) {
        RecipeSuggestion emptyRecipe = new RecipeSuggestion(
                "Pantry is Empty",
                "Add some ingredients to your pantry to get recipe suggestions",
                new ArrayList<>(),
                Arrays.asList("1. Go to your pantry page", "2. Add ingredients you have", "3. Try generating recipes again"),
                0,
                request.getServings() != null ? request.getServings() : 0,
                "easy",
                "General",
                0.0,
                new ArrayList<>()
        );

        return new RecipeGenerationResponse(
                Collections.singletonList(emptyRecipe),
                "empty-pantry-" + UUID.randomUUID().toString(),
                System.currentTimeMillis()
        );
    }

    private List<PantryItemDTO> prioritizeExpiringItems(List<PantryItemDTO> allItems,
            List<PantryItemDTO> expiringItems) {
        Set<String> expiringNames = expiringItems.stream()
                .map(PantryItemDTO::getName)
                .collect(Collectors.toSet());

        List<PantryItemDTO> prioritized = new ArrayList<>();

        prioritized.addAll(allItems.stream()
                .filter(item -> expiringNames.contains(item.getName()))
                .collect(Collectors.toList()));

        prioritized.addAll(allItems.stream()
                .filter(item -> !expiringNames.contains(item.getName()))
                .collect(Collectors.toList()));

        return prioritized;
    }


    private String categorizeIngredient(String ingredientName) {
        String name = ingredientName.toLowerCase();

        if (name.contains("chicken") || name.contains("beef") || name.contains("pork")
                || name.contains("fish") || name.contains("tofu") || name.contains("egg")) {
            return "protein";
        } else if (name.contains("rice") || name.contains("pasta") || name.contains("noodle")
                || name.contains("bread") || name.contains("flour")) {
            return "grain";
        } else if (name.contains("tomato") || name.contains("onion") || name.contains("garlic")
                || name.contains("carrot") || name.contains("potato") || name.contains("broccoli")) {
            return "vegetable";
        } else if (name.contains("milk") || name.contains("cheese") || name.contains("yogurt")) {
            return "dairy";
        } else if (name.contains("oil") || name.contains("vinegar") || name.contains("sauce")) {
            return "condiment";
        } else {
            return "other";
        }
    }

    private List<RecipeSuggestion> generateFallbackRecipes(List<PantryItemDTO> pantryItems,
            RecipeGenerationRequest preferences) {
        List<RecipeSuggestion> fallbacks = new ArrayList<>();

        Map<String, List<PantryItemDTO>> ingredientGroups = pantryItems.stream()
                .collect(Collectors.groupingBy(item -> categorizeIngredient(item.getName())));

        if (ingredientGroups.containsKey("vegetable") && ingredientGroups.containsKey("protein")) {
            fallbacks.add(createStirFryRecipe(pantryItems, preferences));
        }

        if ((ingredientGroups.containsKey("pasta") || ingredientGroups.containsKey("grain"))
                && (ingredientGroups.containsKey("sauce") || ingredientGroups.containsKey("tomato"))) {
            fallbacks.add(createPastaRecipe(pantryItems, preferences));
        }

        if (ingredientGroups.containsKey("vegetable") && ingredientGroups.size() >= 3) {
            fallbacks.add(createSoupRecipe(pantryItems, preferences));
        }

        if (ingredientGroups.containsKey("egg") && ingredientGroups.containsKey("vegetable")) {
            fallbacks.add(createOmeletteRecipe(pantryItems, preferences));
        }

        if (ingredientGroups.containsKey("rice") && ingredientGroups.containsKey("vegetable")) {
            fallbacks.add(createFriedRiceRecipe(pantryItems, preferences));
        }

        if (fallbacks.isEmpty() && !pantryItems.isEmpty()) {
            fallbacks.add(createSimpleBowlRecipe(pantryItems, preferences));
        }

        return fallbacks;
    }
    private RecipeSuggestion createStirFryRecipe(List<PantryItemDTO> pantryItems,
                                            RecipeGenerationRequest preferences) {
    List<PantryItemDTO> vegetables = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("vegetable"))
        .limit(3)
        .collect(Collectors.toList());
    
    List<PantryItemDTO> proteins = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("protein"))
        .limit(1)
        .collect(Collectors.toList());
    
    List<IngredientDTO> ingredients = new ArrayList<>();
    
    vegetables.forEach(veg -> 
        ingredients.add(new IngredientDTO(veg.getName(), 
            Math.min(veg.getQuantity(), 200.0), "g")));
    
    proteins.forEach(protein -> 
        ingredients.add(new IngredientDTO(protein.getName(), 
            Math.min(protein.getQuantity(), 150.0), "g")));
    
    return new RecipeSuggestion(
        "Quick Vegetable Stir Fry",
        "A quick and healthy stir fry using your available vegetables and protein",
        ingredients,
        Arrays.asList(
            "Chop all vegetables into bite-sized pieces",
            "Cut protein into thin strips or cubes",
            "Heat 1 tablespoon of oil in a wok or large pan over high heat",
            "Cook protein first until browned, then remove from pan",
            "Add vegetables and stir fry for 3-5 minutes until tender-crisp",
            "Return protein to pan, add 2 tablespoons of soy sauce",
            "Stir fry for another 1-2 minutes until everything is heated through",
            "Serve hot over rice or noodles if available"
        ),
        20,
        preferences.getServings() != null ? preferences.getServings() : 2,
        "easy",
        "Asian",
        0.7,
        Arrays.asList("cooking oil", "soy sauce", "garlic")
    );
}

private RecipeSuggestion createPastaRecipe(List<PantryItemDTO> pantryItems,
                                          RecipeGenerationRequest preferences) {
    Optional<PantryItemDTO> pasta = pantryItems.stream()
        .filter(item -> item.getName().toLowerCase().contains("pasta") || 
                       item.getName().toLowerCase().contains("spaghetti") ||
                       item.getName().toLowerCase().contains("noodle"))
        .findFirst();
    
    List<PantryItemDTO> sauceIngredients = pantryItems.stream()
        .filter(item -> item.getName().toLowerCase().contains("tomato") ||
                       item.getName().toLowerCase().contains("sauce") ||
                       item.getName().toLowerCase().contains("cream"))
        .limit(3)
        .collect(Collectors.toList());
    
    List<IngredientDTO> ingredients = new ArrayList<>();
    
    if (pasta.isPresent()) {
        ingredients.add(new IngredientDTO(pasta.get().getName(), 
            Math.min(pasta.get().getQuantity(), 200.0), "g"));
    } else {
        pantryItems.stream()
            .filter(item -> categorizeIngredient(item.getName()).equals("grain"))
            .findFirst()
            .ifPresent(grain -> 
                ingredients.add(new IngredientDTO(grain.getName(), 
                    Math.min(grain.getQuantity(), 200.0), "g")));
    }
    
    sauceIngredients.forEach(ing -> 
        ingredients.add(new IngredientDTO(ing.getName(), 
            Math.min(ing.getQuantity(), 150.0), "g")));
    
    pantryItems.stream()
        .filter(item -> item.getName().toLowerCase().contains("cheese"))
        .findFirst()
        .ifPresent(cheese -> 
            ingredients.add(new IngredientDTO(cheese.getName(), 
                Math.min(cheese.getQuantity(), 50.0), "g")));
    
    return new RecipeSuggestion(
        "Simple Pasta Dish",
        pasta.isPresent() ? "A delicious pasta dish with your available ingredients" 
                         : "Grain-based dish with flavorful sauce",
        ingredients,
        Arrays.asList(
            "Cook the pasta/grain according to package instructions",
            "While pasta cooks, chop sauce ingredients if needed",
            "Heat 1 tablespoon of oil in a pan over medium heat",
            "Add sauce ingredients and cook for 5-7 minutes until softened",
            "Season with salt, pepper, and herbs if available",
            "Drain pasta/grain and add to the sauce",
            "Toss everything together until well coated",
            "Grate cheese on top if available and serve immediately"
        ),
        25,
        preferences.getServings() != null ? preferences.getServings() : 2,
        "easy",
        "Italian",
        0.6,
        Arrays.asList("salt", "pepper", "olive oil", "herbs")
    );
}

private RecipeSuggestion createSoupRecipe(List<PantryItemDTO> pantryItems,
                                         RecipeGenerationRequest preferences) {
    List<PantryItemDTO> vegetables = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("vegetable"))
        .limit(4)
        .collect(Collectors.toList());
    
    Optional<PantryItemDTO> protein = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("protein"))
        .findFirst();
    
    List<IngredientDTO> ingredients = new ArrayList<>();
    
    vegetables.forEach(veg -> 
        ingredients.add(new IngredientDTO(veg.getName(), 
            Math.min(veg.getQuantity(), 150.0), "g")));
    
    protein.ifPresent(p -> 
        ingredients.add(new IngredientDTO(p.getName(), 
            Math.min(p.getQuantity(), 100.0), "g")));
    
    pantryItems.stream()
        .filter(item -> item.getName().toLowerCase().contains("broth") ||
                       item.getName().toLowerCase().contains("stock"))
        .findFirst()
        .ifPresent(broth -> 
            ingredients.add(new IngredientDTO(broth.getName(), 
                Math.min(broth.getQuantity(), 500.0), "ml")));
    
    return new RecipeSuggestion(
        "Hearty Vegetable Soup",
        protein.isPresent() ? "Nourishing soup with vegetables and protein" 
                           : "Simple vegetable soup",
        ingredients,
        Arrays.asList(
            "Chop all vegetables into bite-sized pieces",
            protein.map(p -> "Cut " + p.getName() + " into small cubes").orElse(""),
            "Heat 1 tablespoon of oil in a large pot over medium heat",
            "Add vegetables (and protein if using) and cook for 5 minutes",
            "Add 4 cups of water or broth to the pot",
            "Bring to a boil, then reduce heat and simmer for 20-25 minutes",
            "Season with salt and pepper to taste",
            "Serve hot with crusty bread if available"
        ).stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()),
        35,
        preferences.getServings() != null ? preferences.getServings() : 4,
        "easy",
        "International",
        0.8,
        Arrays.asList("salt", "pepper", "herbs", "broth/stock")
    );
}

private RecipeSuggestion createOmeletteRecipe(List<PantryItemDTO> pantryItems,
                                             RecipeGenerationRequest preferences) {
    List<PantryItemDTO> eggs = pantryItems.stream()
        .filter(item -> item.getName().toLowerCase().contains("egg"))
        .collect(Collectors.toList());
    
    List<PantryItemDTO> fillings = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("vegetable") ||
                       item.getName().toLowerCase().contains("cheese") ||
                       item.getName().toLowerCase().contains("ham"))
        .limit(3)
        .collect(Collectors.toList());
    
    List<IngredientDTO> ingredients = new ArrayList<>();
    
    if (!eggs.isEmpty()) {
        ingredients.add(new IngredientDTO("Eggs", 
            Math.min(eggs.get(0).getQuantity(), 3.0), "pieces"));
    }
    
    fillings.forEach(filling -> 
        ingredients.add(new IngredientDTO(filling.getName(), 
            Math.min(filling.getQuantity(), 50.0), "g")));
    
    return new RecipeSuggestion(
        "Custom Omelette",
        "Fluffy omelette filled with your available ingredients",
        ingredients,
        Arrays.asList(
            "Chop filling ingredients into small pieces",
            "Beat eggs in a bowl with a pinch of salt and pepper",
            "Heat 1 teaspoon of butter or oil in a non-stick pan over medium heat",
            "Add filling ingredients and cook for 2-3 minutes until softened",
            "Pour beaten eggs over the fillings",
            "Cook for 2-3 minutes until edges set, then gently lift edges",
            "When top is nearly set, fold omelette in half",
            "Slide onto plate and serve immediately"
        ),
        15,
        1,
        "easy",
        "French",
        0.9,
        Arrays.asList("butter/oil", "salt", "pepper")
    );
}

private RecipeSuggestion createFriedRiceRecipe(List<PantryItemDTO> pantryItems,
                                              RecipeGenerationRequest preferences) {
    Optional<PantryItemDTO> rice = pantryItems.stream()
        .filter(item -> item.getName().toLowerCase().contains("rice"))
        .findFirst();
    
    List<PantryItemDTO> mixins = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("vegetable") ||
                       categorizeIngredient(item.getName()).equals("protein") ||
                       item.getName().toLowerCase().contains("egg"))
        .limit(4)
        .collect(Collectors.toList());
    
    List<IngredientDTO> ingredients = new ArrayList<>();
    
    rice.ifPresent(r -> 
        ingredients.add(new IngredientDTO(r.getName(), 
            Math.min(r.getQuantity(), 300.0), "g")));
    
    mixins.forEach(mixin -> 
        ingredients.add(new IngredientDTO(mixin.getName(), 
            Math.min(mixin.getQuantity(), 100.0), "g")));
    
    return new RecipeSuggestion(
        "Fried Rice",
        "Quick and versatile fried rice using leftover rice and available ingredients",
        ingredients,
        Arrays.asList(
            "If rice is freshly cooked, spread it on a plate to cool slightly",
            "Chop all mix-in ingredients into small, uniform pieces",
            "Heat 2 tablespoons of oil in a wok or large pan over high heat",
            "Add protein (if using) and cook until done, then remove",
            "Add vegetables and cook for 2-3 minutes until tender-crisp",
            "Push vegetables to one side, add beaten eggs if using and scramble",
            "Add rice and break up any clumps",
            "Add 2 tablespoons of soy sauce and stir fry for 2-3 minutes",
            "Return protein to pan and mix everything together",
            "Serve hot"
        ),
        20,
        preferences.getServings() != null ? preferences.getServings() : 2,
        "easy",
        "Asian",
        0.7,
        Arrays.asList("cooking oil", "soy sauce", "garlic")
    );
}

private RecipeSuggestion createSimpleBowlRecipe(List<PantryItemDTO> pantryItems,
                                               RecipeGenerationRequest preferences) {
    List<PantryItemDTO> grains = pantryItems.stream()
        .filter(item -> categorizeIngredient(item.getName()).equals("grain") ||
                       item.getName().toLowerCase().contains("rice") ||
                       item.getName().toLowerCase().contains("quinoa"))
        .limit(1)
        .collect(Collectors.toList());
    
    List<PantryItemDTO> toppings = pantryItems.stream()
        .filter(item -> !categorizeIngredient(item.getName()).equals("grain"))
        .limit(5)
        .collect(Collectors.toList());
    
    List<IngredientDTO> ingredients = new ArrayList<>();
    
    if (!grains.isEmpty()) {
        ingredients.add(new IngredientDTO(grains.get(0).getName(), 
            Math.min(grains.get(0).getQuantity(), 150.0), "g"));
    }
    
    toppings.forEach(topping -> 
        ingredients.add(new IngredientDTO(topping.getName(), 
            Math.min(topping.getQuantity(), 75.0), "g")));
    
    return new RecipeSuggestion(
        "Simple Grain Bowl",
        "Customizable bowl with grains and your available toppings",
        ingredients,
        Arrays.asList(
            grains.isEmpty() ? "Arrange your ingredients attractively on a plate" 
                            : "Cook grain according to package instructions",
            "Prepare toppings: chop vegetables, cook protein if needed",
            "If using dressing, whisk together 3 parts oil to 1 part acid (vinegar/lemon)",
            "Place grain in bowl (if using) and arrange toppings on top",
            "Drizzle with dressing or sauce if available",
            "Season with salt and pepper to taste"
        ),
        15,
        preferences.getServings() != null ? preferences.getServings() : 1,
        "very easy",
        "International",
        0.95,
        Arrays.asList("salt", "pepper", "dressing ingredients")
    );
}
}
