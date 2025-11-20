package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.file.ConfigurationSource.Resource;
import org.postgresql.jdbc.FieldMetadata;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.service.FileService;

import lombok.RequiredArgsConstructor;

// FileController.java
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService service;

    @PostMapping
    public ResponseEntity<FieldMetadata> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @AuthenticationPrincipal Jwt jwt) {  // or custom UserPrincipal
        Long userId = Long.valueOf(jwt.getClaim("sub"));  // adjust claim name
        FieldMetadata meta = service.store(file, projectId, userId);
        return ResponseEntity.ok(meta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @AuthenticationPrincipal Jwt jwt) throws IOException {
        File file = service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!file.getOwnerId().equals(Long.valueOf(jwt.getClaim("sub")))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Resource resource = new FileSystemResource(Paths.get(file.getPath()).toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(file.getMimeType()))
                .contentLength(file.getSize())
                .body(resource);
    }

    @GetMapping
    public List<FileMetadata> list(@RequestParam Optional<Long> projectId,
                                   @AuthenticationPrincipal Jwt jwt) {
        if (projectId.isPresent()) {
            return service.findByProjectId(projectId.get(), Long.valueOf(jwt.getClaim("sub")));
        }
        return service.findAllByOwnerId(Long.valueOf(jwt.getClaim("sub")));
    }
}
