package com.gymapp.backend.dto;

import com.gymapp.backend.enums.WorkoutStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class WorkoutRequestDTO {

    @NotBlank(message = "Workout name is required")
    private String name;

    @NotNull(message = "Date is required")
    private LocalDateTime date;

    private UUID routineId; // Nullable intentionally: Populated if generated from a blueprint
    private UUID phaseId;   // Nullable intentionally: Populated if tied to a phase

    @NotNull(message = "Workout status is required")
    private WorkoutStatus status; // Explicitly requires the frontend to define the state

    @Valid
    @NotNull(message = "Exercises list cannot be null")
    private List<ExerciseRequestDTO> exercises;

    // Getters and Setters
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

    public List<ExerciseRequestDTO> getExercises() { return exercises; }
    public void setExercises(List<ExerciseRequestDTO> exercises) { this.exercises = exercises; }
}