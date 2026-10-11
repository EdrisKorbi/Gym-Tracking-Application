package com.gymapp.backend.service;

import com.gymapp.backend.dto.*;
import com.gymapp.backend.entity.*;
import com.gymapp.backend.repository.ProgramRepository;
import com.gymapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final UserRepository userRepository;

    public ProgramService(ProgramRepository programRepository, UserRepository userRepository) {
        this.programRepository = programRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProgramResponseDTO createProgram(String email, ProgramRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Program program = new Program();
        program.setUser(user);
        program.setName(requestDTO.getName());
        program.setDescription(requestDTO.getDescription());
        program.setIsTemplate(requestDTO.getIsTemplate());

        for (ProgramPhaseRequestDTO phaseDto : requestDTO.getPhases()) {
            ProgramPhase phase = new ProgramPhase();
            phase.setProgram(program);
            phase.setName(phaseDto.getName());
            phase.setPhaseOrder(phaseDto.getPhaseOrder());

            for (RoutineRequestDTO routineDto : phaseDto.getRoutines()) {
                Routine routine = new Routine();
                routine.setPhase(phase);
                routine.setName(routineDto.getName());
                routine.setDayNumber(routineDto.getDayNumber());

                for (RoutineExerciseRequestDTO targetDto : routineDto.getRoutineExercises()) {
                    RoutineExercise target = new RoutineExercise();
                    target.setRoutine(routine);
                    target.setName(targetDto.getName());
                    target.setTargetSets(targetDto.getTargetSets());
                    target.setTargetReps(targetDto.getTargetReps());
                    target.setExerciseOrder(targetDto.getExerciseOrder());

                    routine.getRoutineExercises().add(target);
                }
                phase.getRoutines().add(routine);
            }
            program.getPhases().add(phase);
        }

        Program savedProgram = programRepository.save(program);

        return mapToResponseDTO(savedProgram);
    }

    @Transactional
    public ProgramResponseDTO updateProgram(String email, UUID programId, ProgramRequestDTO requestDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        if (!program.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this program");
        }

        // 1. Update top-level Program fields
        program.setName(requestDTO.getName());
        program.setDescription(requestDTO.getDescription());
        program.setIsTemplate(requestDTO.getIsTemplate());

        // 2. Diff/Merge Level 1: Program Phases
        Map<UUID, ProgramPhase> phaseMap = program.getPhases().stream()
                .collect(Collectors.toMap(ProgramPhase::getId, p -> p));
        program.getPhases().clear();

        for (ProgramPhaseRequestDTO phaseDto : requestDTO.getPhases()) {
            ProgramPhase phase;
            if (phaseDto.getId() != null && phaseMap.containsKey(phaseDto.getId())) {
                phase = phaseMap.get(phaseDto.getId()); // UPDATE existing
                phase.setName(phaseDto.getName());
                phase.setPhaseOrder(phaseDto.getPhaseOrder());
            } else {
                phase = new ProgramPhase(); // INSERT new
                phase.setProgram(program);
                phase.setName(phaseDto.getName());
                phase.setPhaseOrder(phaseDto.getPhaseOrder());
            }

            // 3. Diff/Merge Level 2: Routines (inside the Phase)
            Map<UUID, Routine> routineMap = phase.getRoutines().stream()
                    .collect(Collectors.toMap(Routine::getId, r -> r));
            phase.getRoutines().clear();

            for (RoutineRequestDTO routineDto : phaseDto.getRoutines()) {
                Routine routine;
                if (routineDto.getId() != null && routineMap.containsKey(routineDto.getId())) {
                    routine = routineMap.get(routineDto.getId()); // UPDATE existing
                    routine.setName(routineDto.getName());
                    routine.setDayNumber(routineDto.getDayNumber());
                } else {
                    routine = new Routine(); // INSERT new
                    routine.setPhase(phase);
                    routine.setName(routineDto.getName());
                    routine.setDayNumber(routineDto.getDayNumber());
                }

                // 4. Diff/Merge Level 3: Routine Exercises (inside the Routine)
                Map<UUID, RoutineExercise> targetMap = routine.getRoutineExercises().stream()
                        .collect(Collectors.toMap(RoutineExercise::getId, t -> t));
                routine.getRoutineExercises().clear();

                for (RoutineExerciseRequestDTO targetDto : routineDto.getRoutineExercises()) {
                    RoutineExercise target;
                    if (targetDto.getId() != null && targetMap.containsKey(targetDto.getId())) {
                        target = targetMap.get(targetDto.getId()); // UPDATE existing
                        target.setName(targetDto.getName());
                        target.setTargetSets(targetDto.getTargetSets());
                        target.setTargetReps(targetDto.getTargetReps());
                        target.setExerciseOrder(targetDto.getExerciseOrder());
                    } else {
                        target = new RoutineExercise(); // INSERT new
                        target.setRoutine(routine);
                        target.setName(targetDto.getName());
                        target.setTargetSets(targetDto.getTargetSets());
                        target.setTargetReps(targetDto.getTargetReps());
                        target.setExerciseOrder(targetDto.getExerciseOrder());
                    }
                    routine.getRoutineExercises().add(target);
                }
                phase.getRoutines().add(routine);
            }
            program.getPhases().add(phase);
        }

        // Save cascades all updates, inserts, and deletions safely
        Program updatedProgram = programRepository.save(program);
        return mapToResponseDTO(updatedProgram);
    }

    @Transactional
    public void deleteProgram(String email, UUID programId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        if (!program.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this program");
        }

        // Thanks to CascadeType.ALL, this deletes the Phases, Routines, and Targets automatically.
        // Thanks to ON DELETE SET NULL, the user's historical Workouts are kept safe.
        programRepository.delete(program);
    }

    @Transactional(readOnly = true)
    public List<ProgramResponseDTO> getAllPrograms(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetches only the programs belonging to this specific user
        List<Program> programs = programRepository.findByUserId(user.getId());

        // Safe to use streams here because mapping does not mutate database state
        return programs.stream().map(this::mapToResponseDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProgramResponseDTO getProgramById(String email, UUID programId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        // Strict security check: Prevent users from fetching other people's templates
        if (!program.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied: You do not own this program");
        }

        return mapToResponseDTO(program);
    }

    private ProgramResponseDTO mapToResponseDTO(Program program) {
        ProgramResponseDTO response = new ProgramResponseDTO();
        response.setId(program.getId());
        response.setName(program.getName());
        response.setDescription(program.getDescription());
        response.setIsTemplate(program.getIsTemplate());

        List<ProgramPhaseResponseDTO> phaseDTOs = new ArrayList<>();
        for (ProgramPhase phase : program.getPhases()) {
            ProgramPhaseResponseDTO phaseDTO = new ProgramPhaseResponseDTO();
            phaseDTO.setId(phase.getId());
            phaseDTO.setName(phase.getName());
            phaseDTO.setPhaseOrder(phase.getPhaseOrder());

            List<RoutineResponseDTO> routineDTOs = new ArrayList<>();
            for (Routine routine : phase.getRoutines()) {
                RoutineResponseDTO routineDTO = new RoutineResponseDTO();
                routineDTO.setId(routine.getId());
                routineDTO.setName(routine.getName());
                routineDTO.setDayNumber(routine.getDayNumber());

                List<RoutineExerciseResponseDTO> targetDTOs = new ArrayList<>();
                for (RoutineExercise target : routine.getRoutineExercises()) {
                    RoutineExerciseResponseDTO targetDTO = new RoutineExerciseResponseDTO();
                    targetDTO.setId(target.getId());
                    targetDTO.setName(target.getName());
                    targetDTO.setTargetSets(target.getTargetSets());
                    targetDTO.setTargetReps(target.getTargetReps());
                    targetDTO.setExerciseOrder(target.getExerciseOrder());

                    targetDTOs.add(targetDTO);
                }
                routineDTO.setRoutineExercises(targetDTOs);
                routineDTOs.add(routineDTO);
            }
            phaseDTO.setRoutines(routineDTOs);
            phaseDTOs.add(phaseDTO);
        }
        response.setPhases(phaseDTOs);

        return response;
    }
}