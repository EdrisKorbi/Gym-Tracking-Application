package com.gymapp.backend.repository;

import com.gymapp.backend.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID> {

    Page<Workout> findByUserId(UUID userId, Pageable pageable);

    // Safe to use because it only fetches a single sub-list (Exercises)
    @EntityGraph(attributePaths = {"exercises"})
    @Query("SELECT w FROM Workout w WHERE w.id = :id")
    Optional<Workout> findByIdWithExercises(@Param("id") UUID id);
}