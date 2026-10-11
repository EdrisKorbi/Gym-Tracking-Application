package com.gymapp.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ExerciseRequestDTO {

    // NEW FIELD: Used to identify existing exercises during a PUT request
    private UUID id;

    @NotBlank(message = "Exercise name is required")
    private String name;

    @NotNull(message = "Sets are required")
    @Min(value = 1, message = "Must have at least 1 set")
    private Integer sets;

    @NotNull(message = "Reps are required")
    @Min(value = 1, message = "Must have at least 1 rep")
    private Integer reps;

    @NotNull(message = "Weight is required")
    @Min(value = 0, message = "Weight cannot be negative")
    private Double weight;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
}