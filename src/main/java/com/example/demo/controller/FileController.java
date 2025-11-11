// FileController.java
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService service;

    @PostMapping
    public ResponseEntity<FileMetadata> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("projectId") Long projectId,
            @AuthenticationPrincipal Jwt jwt) {  // or custom UserPrincipal
        Long userId = Long.valueOf(jwt.getClaim("sub"));  // adjust claim name
        FileMetadata meta = service.store(file, projectId, userId);
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
