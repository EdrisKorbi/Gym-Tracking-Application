package com.gymapp.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class WorkoutResponseDTO {
    private UUID id;
    private String name;
    private LocalDateTime date;
    private List<ExerciseResponseDTO> exercises;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public List<ExerciseResponseDTO> getExercises() { return exercises; }
    public void setExercises(List<ExerciseResponseDTO> exercises) { this.exercises = exercises; }
}