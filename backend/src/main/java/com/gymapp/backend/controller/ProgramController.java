package com.gymapp.backend.controller;

import com.gymapp.backend.config.CustomUserDetails;
import com.gymapp.backend.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> generateWorkouts(
            @PathVariable UUID id,
            @Valid @RequestBody WorkoutGenerationRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<Workout> generated = workoutGenerationService.generateWorkoutsFromProgram(id, currentUser.getId(), requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Successfully scheduled " + generated.size() + " workouts on your calendar.")
        );
    }

    // ==========================================
    // HIGH-PERFORMANCE CRUD OPERATIONS
    // ==========================================

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProgramResponseDTO>>> getAllPrograms(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<ProgramResponseDTO> programs = programService.getAllPrograms(currentUser.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Programs retrieved successfully", programs)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramResponseDTO>> getProgramById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO program = programService.getProgramById(currentUser.getId(), id);
        return ResponseEntity.ok(
                ApiResponse.success("Program retrieved successfully", program)
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProgramResponseDTO>> createProgram(
            @Valid @RequestBody ProgramRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO response = programService.createProgram(currentUser.getId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Program created successfully", response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProgramResponseDTO>> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody ProgramRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO response = programService.updateProgram(currentUser.getId(), id, requestDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Program updated successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        programService.deleteProgram(currentUser.getId(), id);
        return ResponseEntity.ok(
                ApiResponse.success("Program deleted successfully")
        );
    }

    // 1. Browse Master Templates (Available to all authenticated users)
    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<ProgramResponseDTO>>> getAllTemplates() {
        List<ProgramResponseDTO> templates = programService.getAllTemplates();
        return ResponseEntity.ok(
                ApiResponse.success("Templates retrieved successfully", templates)
        );
    }

    // 2. Clone a Template to the user's personal account
    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<ProgramResponseDTO>> cloneTemplate(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        ProgramResponseDTO clonedProgram = programService.cloneProgram(id, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Template cloned successfully", clonedProgram)
        );
    }
}