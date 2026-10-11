package com.gymapp.backend.controller;

import com.gymapp.backend.dto.WorkoutRequestDTO;
import com.gymapp.backend.dto.WorkoutResponseDTO;
import com.gymapp.backend.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    public ResponseEntity<WorkoutResponseDTO> logWorkout(
            @Valid @RequestBody WorkoutRequestDTO requestDTO,
            Principal principal) {

        WorkoutResponseDTO response = workoutService.logWorkout(principal.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkoutResponseDTO>> getAllWorkouts(Principal principal) {
        List<WorkoutResponseDTO> workouts = workoutService.getAllWorkouts(principal.getName());
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> getWorkoutById(
            @PathVariable UUID id,
            Principal principal) {

        WorkoutResponseDTO workout = workoutService.getWorkoutById(principal.getName(), id);
        return ResponseEntity.ok(workout);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> updateWorkout(
            @PathVariable UUID id,
            @Valid @RequestBody WorkoutRequestDTO requestDTO,
            Principal principal) {

        WorkoutResponseDTO response = workoutService.updateWorkout(principal.getName(), id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(
            @PathVariable UUID id,
            Principal principal) {

        workoutService.deleteWorkout(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}