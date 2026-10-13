package com.gymapp.backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "program_phases")
public class ProgramPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "phase_order", nullable = false)
    private Integer phaseOrder;

    @OneToMany(mappedBy = "phase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Routine> routines = new ArrayList<>();

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPhaseOrder() { return phaseOrder; }
    public void setPhaseOrder(Integer phaseOrder) { this.phaseOrder = phaseOrder; }

    // These are the missing methods your IDE is complaining about
    public List<Routine> getRoutines() { return routines; }
    public void setRoutines(List<Routine> routines) { this.routines = routines; }
}