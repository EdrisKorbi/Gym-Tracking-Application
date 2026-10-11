package com.gymapp.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class ProgramPhaseRequestDTO {

    private UUID id;

    @NotBlank(message = "Phase name is required")
    private String name;

    @NotNull(message = "Phase order is required")
    @Min(value = 1, message = "Phase order must be at least 1")
    private Integer phaseOrder;

    @Valid
    @NotNull(message = "Routines list cannot be null")
    private List<RoutineRequestDTO> routines;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPhaseOrder() { return phaseOrder; }
    public void setPhaseOrder(Integer phaseOrder) { this.phaseOrder = phaseOrder; }

    public List<RoutineRequestDTO> getRoutines() { return routines; }
    public void setRoutines(List<RoutineRequestDTO> routines) { this.routines = routines; }
}