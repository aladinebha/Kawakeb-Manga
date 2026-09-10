package studio.manga.documents; import java.util.*; import org.springframework.data.jpa.repository.JpaRepository;
interface ChapterRepository extends JpaRepository<Chapter,UUID>{ List<Chapter> findByProjectIdOrderByPosition(UUID projectId); }
interface DocumentRepository extends JpaRepository<Document,UUID>{ List<Document> findByProjectIdOrderByUpdatedAtDesc(UUID projectId); Optional<Document> findByIdAndProjectId(UUID id,UUID projectId); }
interface DocumentVersionRepository extends JpaRepository<DocumentVersion,UUID>{ List<DocumentVersion> findByDocumentIdOrderByRevisionDesc(UUID documentId); }
