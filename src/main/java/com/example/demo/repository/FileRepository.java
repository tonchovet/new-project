// FileRepository.java
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByProjectId(Long projectId);
}
