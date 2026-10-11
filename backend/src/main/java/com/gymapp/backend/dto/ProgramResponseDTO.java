package com.gymapp.backend.dto;

import java.util.List;
import java.util.UUID;

public class ProgramResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private Boolean isTemplate;
    private List<ProgramPhaseResponseDTO> phases;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsTemplate() { return isTemplate; }
    public void setIsTemplate(Boolean isTemplate) { this.isTemplate = isTemplate; }

    public List<ProgramPhaseResponseDTO> getPhases() { return phases; }
    public void setPhases(List<ProgramPhaseResponseDTO> phases) { this.phases = phases; }
}