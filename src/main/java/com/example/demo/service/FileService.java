package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.postgresql.jdbc.FieldMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.config.FileStorageProperties;
import com.example.demo.domain.File;
import com.example.demo.repository.FileRepository;

// FileService.java
@Service
public class FileService {
    private final FileRepository repo;
    private final Path uploadDir;

    @Autowired
    public FileService(FileRepository repo, FileStorageProperties props) {
        this.repo = repo;
        this.uploadDir = Paths.get(props.getUploadDir());
        Files.createDirectories(this.uploadDir);
    }

    public FieldMetadata store(MultipartFile file, Long projectId, Long userId) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String storageName = uuid + (ext.isEmpty() ? "" : "." + ext);
        Path target = uploadDir.resolve(storageName);

        try (InputStream in = file.getInputStream();
             OutputStream out = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
            StreamUtils.copy(in, out);
        }

        File meta = new File();
        meta.setOriginalName(file.getOriginalFilename());
        meta.setStorageName(storageName);
        meta.setMimeType(file.getContentType());
        meta.setSize(file.getSize());
        meta.setPath(target.toString());
        meta.setProjectId(projectId);
        meta.setOwnerId(userId);
        meta.setCreatedAt(LocalDateTime.now());

        return repo.saveAll(meta);
    }

    public Optional<File> findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }
}
