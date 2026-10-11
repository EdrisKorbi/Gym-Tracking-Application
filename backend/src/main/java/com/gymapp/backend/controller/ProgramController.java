package com.gymapp.backend.controller;

import com.gymapp.backend.dto.ProgramRequestDTO;
import com.gymapp.backend.dto.ProgramResponseDTO;
import com.gymapp.backend.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public ResponseEntity<List<ProgramResponseDTO>> getAllPrograms(Principal principal) {
        List<ProgramResponseDTO> programs = programService.getAllPrograms(principal.getName());
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramResponseDTO> getProgramById(
            @PathVariable UUID id,
            Principal principal) {
        ProgramResponseDTO program = programService.getProgramById(principal.getName(), id);
        return ResponseEntity.ok(program);
    }

    @PostMapping
    public ResponseEntity<ProgramResponseDTO> createProgram(
            @Valid @RequestBody ProgramRequestDTO requestDTO,
            Principal principal) {

        // Extract email from JWT and pass the DTO to the service
        ProgramResponseDTO response = programService.createProgram(principal.getName(), requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProgramResponseDTO> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody ProgramRequestDTO requestDTO,
            Principal principal) {

        ProgramResponseDTO response = programService.updateProgram(principal.getName(), id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable UUID id,
            Principal principal) {

        programService.deleteProgram(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}