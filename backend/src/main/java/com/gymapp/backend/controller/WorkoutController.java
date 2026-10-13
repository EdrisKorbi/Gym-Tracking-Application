package com.gymapp.backend.controller;

import com.gymapp.backend.config.CustomUserDetails;
import com.gymapp.backend.dto.ApiResponse;
import com.gymapp.backend.dto.WorkoutRequestDTO;
import com.gymapp.backend.dto.WorkoutResponseDTO;
import com.gymapp.backend.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutResponseDTO>> logWorkout(
            @Valid @RequestBody WorkoutRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        WorkoutResponseDTO response = workoutService.logWorkout(currentUser.getId(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Workout logged successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkoutResponseDTO>>> getAllWorkouts(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        List<WorkoutResponseDTO> workouts = workoutService.getAllWorkouts(currentUser.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Workouts retrieved successfully", workouts)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutResponseDTO>> getWorkoutById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        WorkoutResponseDTO workout = workoutService.getWorkoutById(currentUser.getId(), id);
        return ResponseEntity.ok(
                ApiResponse.success("Workout retrieved successfully", workout)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutResponseDTO>> updateWorkout(
            @PathVariable UUID id,
            @Valid @RequestBody WorkoutRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        WorkoutResponseDTO response = workoutService.updateWorkout(currentUser.getId(), id, requestDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Workout updated successfully", response)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorkout(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        workoutService.deleteWorkout(currentUser.getId(), id);
        return ResponseEntity.ok(
                ApiResponse.success("Workout deleted successfully")
        );
    }
}