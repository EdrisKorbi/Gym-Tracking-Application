package com.gymapp.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class WorkoutRequestDTO {

    @NotBlank(message = "Workout name is required")
    private String name;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    @Valid
    @NotNull(message = "Exercises list cannot be null")
    private List<ExerciseRequestDTO> exercises;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public List<ExerciseRequestDTO> getExercises() { return exercises; }
    public void setExercises(List<ExerciseRequestDTO> exercises) { this.exercises = exercises; }
}