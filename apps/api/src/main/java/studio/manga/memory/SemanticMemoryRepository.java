package studio.manga.memory;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemanticMemoryRepository extends JpaRepository<SemanticMemoryChunk, UUID> {
  List<SemanticMemoryChunk> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
  List<SemanticMemoryChunk> findByProjectIdAndContentContainingIgnoreCase(UUID projectId, String query);
  void deleteByProjectIdAndSourceDocumentId(UUID projectId, UUID sourceDocumentId);
}
