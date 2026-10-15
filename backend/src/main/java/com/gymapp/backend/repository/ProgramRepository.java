package com.gymapp.backend.repository;

import com.gymapp.backend.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProgramRepository extends JpaRepository<Program, UUID> {
    Page<Program> findByUserId(UUID userId, Pageable pageable);
    Page<Program> findByIsTemplateTrue(Pageable pageable);
}