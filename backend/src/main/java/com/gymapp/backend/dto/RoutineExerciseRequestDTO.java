package com.gymapp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class RoutineExerciseRequestDTO {

    private UUID id; // Used later for PUT/Updates

    @NotBlank(message = "Exercise name is required")
    private String name;

    @NotNull(message = "Target sets are required")
    @Min(value = 1, message = "Must have at least 1 target set")
    private Integer targetSets;

    @NotNull(message = "Target reps are required")
    @Min(value = 1, message = "Must have at least 1 target rep")
    private Integer targetReps;

    @NotNull(message = "Exercise order is required")
    @Min(value = 0, message = "Exercise order cannot be negative")
    private Integer exerciseOrder;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getTargetSets() { return targetSets; }
    public void setTargetSets(Integer targetSets) { this.targetSets = targetSets; }

    public Integer getTargetReps() { return targetReps; }
    public void setTargetReps(Integer targetReps) { this.targetReps = targetReps; }

    public Integer getExerciseOrder() { return exerciseOrder; }
    public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }
}