package com.gymapp.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProgramRequestDTO {

    @NotBlank(message = "Program name is required")
    private String name;

    private String description; // Optional, no validation needed

    private Boolean isTemplate = false; // Optional, defaults to false

    @Valid
    @NotNull(message = "Phases list cannot be null")
    private List<ProgramPhaseRequestDTO> phases;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsTemplate() { return isTemplate; }
    public void setIsTemplate(Boolean isTemplate) { this.isTemplate = isTemplate; }

    public List<ProgramPhaseRequestDTO> getPhases() { return phases; }
    public void setPhases(List<ProgramPhaseRequestDTO> phases) { this.phases = phases; }
}