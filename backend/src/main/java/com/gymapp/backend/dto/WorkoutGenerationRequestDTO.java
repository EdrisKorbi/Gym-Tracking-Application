package com.gymapp.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class WorkoutGenerationRequestDTO {

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotEmpty(message = "You must select at least one workout day")
    private List<DayOfWeek> preferredDays;

    // Default constructor for Jackson serialization
    public WorkoutGenerationRequestDTO() {}

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public List<DayOfWeek> getPreferredDays() {
        return preferredDays;
    }

    public void setPreferredDays(List<DayOfWeek> preferredDays) {
        this.preferredDays = preferredDays;
    }
}