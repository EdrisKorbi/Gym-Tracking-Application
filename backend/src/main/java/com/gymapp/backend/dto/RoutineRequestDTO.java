package com.gymapp.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class RoutineRequestDTO {

    private UUID id;

    @NotBlank(message = "Routine name is required")
    private String name;

    @NotNull(message = "Day number is required")
    @Min(value = 1, message = "Day number must be at least 1")
    private Integer dayNumber;

    @Valid // Crucial: Tells Spring to validate the nested objects inside this list
    @NotNull(message = "Routine exercises list cannot be null")
    private List<RoutineExerciseRequestDTO> routineExercises;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }

    public List<RoutineExerciseRequestDTO> getRoutineExercises() { return routineExercises; }
    public void setRoutineExercises(List<RoutineExerciseRequestDTO> routineExercises) { this.routineExercises = routineExercises; }
}