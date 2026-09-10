package studio.manga.documents;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
  List<Document> findByProjectIdOrderByUpdatedAtDesc(UUID projectId);
  Optional<Document> findByIdAndProjectId(UUID id, UUID projectId);
}
