// File.java
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
    @Column(nullable = false)
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
}
