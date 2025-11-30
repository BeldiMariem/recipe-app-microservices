package com.recipe.recipe_service.repository;

import com.recipe.recipe_service.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    
    @Query("SELECT r FROM Recipe r WHERE r.visibility = com.recipe.recipe_service.entity.Visibility.PUBLIC OR r.userId = :userId")
    List<Recipe> findVisibleRecipes(@Param("userId") String userId);
    
    List<Recipe> findByUserId(String userId);
    
    Optional<Recipe> findByIdAndUserId(Long id, String userId);
    
    @Query("SELECT r FROM Recipe r WHERE r.visibility = com.recipe.recipe_service.entity.Visibility.PUBLIC")
    List<Recipe> findPublicRecipes();
    
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE " +
           "LOWER(i.name) IN :ingredientNames AND r.visibility = com.recipe.recipe_service.entity.Visibility.PUBLIC")
    List<Recipe> findByIngredientNames(@Param("ingredientNames") List<String> ingredientNames);
    
    @Query("SELECT DISTINCT r FROM Recipe r JOIN r.ingredients i WHERE " +
           "LOWER(i.name) IN :ingredientNames AND (r.visibility = com.recipe.recipe_service.entity.Visibility.PUBLIC OR r.userId = :userId)")
    List<Recipe> findByIngredientNamesAndUser(@Param("ingredientNames") List<String> ingredientNames, @Param("userId") String userId);
}