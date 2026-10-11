package com.gymapp.backend.dto;

import java.util.List;
import java.util.UUID;

public class RoutineResponseDTO {

    private UUID id;
    private String name;
    private Integer dayNumber;
    private List<RoutineExerciseResponseDTO> routineExercises;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }

    public List<RoutineExerciseResponseDTO> getRoutineExercises() { return routineExercises; }
    public void setRoutineExercises(List<RoutineExerciseResponseDTO> routineExercises) { this.routineExercises = routineExercises; }
}