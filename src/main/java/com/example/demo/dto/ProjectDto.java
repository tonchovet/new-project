package com.example.demo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProjectDto {
    private String title;
    private String description;
    private java.math.BigDecimal targetAmount;
}
