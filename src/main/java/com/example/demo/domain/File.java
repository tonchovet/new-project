package com.example.demo.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;


import com.example.demo.model.Project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

// File.java
@Data
@Entity
@Table(name = "files")
public class File {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;
    @Column(nullable = false, unique = true)
    private String storageName;
    @Column(nullable = false)
    private String mimeType;
    @Column(name = "size", nullable = false)
    private Long size;

    @Column(nullable = false)
    private String path;   // absolute or relative path on disk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    // getters/setters

    public void setProjectId(Long projectId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setProjectId'");
    }

    public void setOwnerId(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setOwnerId'");
    }
}
