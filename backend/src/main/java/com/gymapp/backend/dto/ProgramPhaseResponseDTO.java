package com.gymapp.backend.dto;

import java.util.List;
import java.util.UUID;

public class ProgramPhaseResponseDTO {

    private UUID id;
    private String name;
    private Integer phaseOrder;
    private List<RoutineResponseDTO> routines;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPhaseOrder() { return phaseOrder; }
    public void setPhaseOrder(Integer phaseOrder) { this.phaseOrder = phaseOrder; }

    public List<RoutineResponseDTO> getRoutines() { return routines; }
    public void setRoutines(List<RoutineResponseDTO> routines) { this.routines = routines; }
}