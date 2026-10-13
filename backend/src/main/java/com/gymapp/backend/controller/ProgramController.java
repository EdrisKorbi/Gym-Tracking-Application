package com.gymapp.backend.controller;

import com.gymapp.backend.config.CustomUserDetails;
import com.gymapp.backend.dto.MessageResponseDTO;
import com.gymapp.backend.dto.ProgramRequestDTO;
import com.gymapp.backend.dto.ProgramResponseDTO;
import com.gymapp.backend.dto.WorkoutGenerationRequestDTO;
import com.gymapp.backend.entity.Workout;
import com.gymapp.backend.service.ProgramService;
import com.gymapp.backend.service.WorkoutGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {

    private final ProgramService programService;
    private final WorkoutGenerationService workoutGenerationService;

    public ProgramController(ProgramService programService, WorkoutGenerationService workoutGenerationService) {
        this.programService = programService;
        this.workoutGenerationService = workoutGenerationService;
    }

    // ==========================================
    // PHASE 4: WORKOUT GENERATION ENGINE
    // ==========================================

    @PostMapping("/{id}/generate")
    public ResponseEntity<MessageResponseDTO> generateWorkouts(
            @PathVariable UUID id,
            @Valid @RequestBody WorkoutGenerationRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<Workout> generated = workoutGenerationService.generateWorkoutsFromProgram(id, currentUser.getId(), requestDTO);

        // Using our new predictable DTO instead of a Map!
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponseDTO("Successfully scheduled " + generated.size() + " workouts on your calendar."));
    }

    // ==========================================
    // HIGH-PERFORMANCE CRUD OPERATIONS
    // ==========================================

    @GetMapping
    public ResponseEntity<List<ProgramResponseDTO>> getAllPrograms(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // NOW PASSING THE UUID DIRECTLY!
        List<ProgramResponseDTO> programs = programService.getAllPrograms(currentUser.getId());
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponseDTO> getProgramById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO program = programService.getProgramById(currentUser.getId(), id);
        return ResponseEntity.ok(program);
    }

    @PostMapping
    public ResponseEntity<ProgramResponseDTO> createProgram(
            @Valid @RequestBody ProgramRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO response = programService.createProgram(currentUser.getId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramResponseDTO> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody ProgramRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO response = programService.updateProgram(currentUser.getId(), id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        programService.deleteProgram(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}