package com.example.demo.service;

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

    public FileMetadata store(MultipartFile file, Long projectId, Long userId) throws IOException {
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

        return repo.save(meta);
    }
}
