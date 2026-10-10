package com.gymapp.backend.controller;

import com.gymapp.backend.dto.WorkoutRequestDTO;
import com.gymapp.backend.dto.WorkoutResponseDTO;
import com.gymapp.backend.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
}