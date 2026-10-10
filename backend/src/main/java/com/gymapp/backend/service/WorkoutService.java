package com.gymapp.backend.service;

import com.gymapp.backend.dto.*;
import com.gymapp.backend.entity.Exercise;
import com.gymapp.backend.entity.User;
import com.gymapp.backend.entity.Workout;
import com.gymapp.backend.repository.ExerciseRepository;
import com.gymapp.backend.repository.UserRepository;
import com.gymapp.backend.repository.WorkoutRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public WorkoutService(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponseDTO logWorkout(String email, WorkoutRequestDTO requestDTO) {
        // 1. Find the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Create and save the Workout
        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(requestDTO.getName());
        workout.setDate(requestDTO.getDate());

        Workout savedWorkout = workoutRepository.save(workout);

        // 3. Create, link, and save the Exercises
        List<Exercise> exercisesToSave = new ArrayList<>();
        List<ExerciseResponseDTO> exerciseResponseList = new ArrayList<>();

        for (ExerciseRequestDTO exDto : requestDTO.getExercises()) {
            Exercise exercise = new Exercise();
            exercise.setWorkout(savedWorkout); // Link to the parent workout
            exercise.setName(exDto.getName());
            exercise.setSets(exDto.getSets());
            exercise.setReps(exDto.getReps());
            exercise.setWeight(exDto.getWeight());
            exercisesToSave.add(exercise);

            // Populate Response DTO while looping
            ExerciseResponseDTO response = new ExerciseResponseDTO();
            response.setName(exercise.getName());
            response.setSets(exercise.getSets());
            response.setReps(exercise.getReps());
            response.setWeight(exercise.getWeight());
            exerciseResponseList.add(response);
        }

        exerciseRepository.saveAll(exercisesToSave);

        // 4. Update Response DTOs with final DB IDs and return
        for (int i = 0; i < exercisesToSave.size(); i++) {
            exerciseResponseList.get(i).setId(exercisesToSave.get(i).getId());
        }

        WorkoutResponseDTO finalResponse = new WorkoutResponseDTO();
        finalResponse.setId(savedWorkout.getId());
        finalResponse.setName(savedWorkout.getName());
        finalResponse.setDate(savedWorkout.getDate());
        finalResponse.setExercises(exerciseResponseList);

        return finalResponse;
    }
}