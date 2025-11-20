package com.example.demo.repository;

import java.io.File;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

// FileRepository.java
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByProjectId(Long projectId);
}
