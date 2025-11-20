package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000)
    private String description;

    private java.math.BigDecimal targetAmount;

    private java.math.BigDecimal collectedAmount = java.math.BigDecimal.ZERO;

    private LocalDateTime createdAt = LocalDateTime.now();
}
