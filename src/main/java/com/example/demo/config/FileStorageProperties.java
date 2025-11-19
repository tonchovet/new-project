package com.example.demo.config;

// FileStorageProperties.java
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    private String uploadDir = "uploads";
    // getters/setters
}
