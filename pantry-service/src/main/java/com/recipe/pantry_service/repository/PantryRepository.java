package com.recipe.pantry_service.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.pantry_service.entity.PantryItem;

@Repository
public interface PantryRepository extends JpaRepository<PantryItem, Long> {
    List<PantryItem> findByUserId(String userId);
    List<PantryItem> findByUserIdAndExpiryDateBefore(String userId, LocalDate date);
    boolean existsByUserIdAndName(String userId, String name);
    Optional<PantryItem> findByIdAndUserId(Long id, String userId);

}