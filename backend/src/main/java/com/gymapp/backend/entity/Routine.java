package com.gymapp.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routines")
public class Routine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phase_id", nullable = false)
    private ProgramPhase phase;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineExercise> routineExercises = new ArrayList<>();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ProgramPhase getPhase() { return phase; }
    public void setPhase(ProgramPhase phase) { this.phase = phase; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }

    public List<RoutineExercise> getRoutineExercises() { return routineExercises; }
    public void setRoutineExercises(List<RoutineExercise> routineExercises) { this.routineExercises = routineExercises; }
}