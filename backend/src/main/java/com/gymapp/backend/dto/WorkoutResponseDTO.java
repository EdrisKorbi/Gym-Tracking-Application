package com.gymapp.backend.dto;

import com.gymapp.backend.enums.WorkoutStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class WorkoutResponseDTO {
    private UUID id;
    private String name;
    private LocalDateTime date;

    private UUID routineId; // NEW
    private UUID phaseId;   // NEW
    private WorkoutStatus status; // NEW

    private List<ExerciseResponseDTO> exercises;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public UUID getRoutineId() { return routineId; }
    public void setRoutineId(UUID routineId) { this.routineId = routineId; }

    public UUID getPhaseId() { return phaseId; }
    public void setPhaseId(UUID phaseId) { this.phaseId = phaseId; }

    public WorkoutStatus getStatus() { return status; }
    public void setStatus(WorkoutStatus status) { this.status = status; }

    public List<ExerciseResponseDTO> getExercises() { return exercises; }
    public void setExercises(List<ExerciseResponseDTO> exercises) { this.exercises = exercises; }
}