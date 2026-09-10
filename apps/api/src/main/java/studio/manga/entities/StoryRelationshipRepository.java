package studio.manga.entities;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRelationshipRepository extends JpaRepository<StoryRelationship, UUID> {
  List<StoryRelationship> findByProjectIdOrderByUpdatedAtDesc(UUID projectId);
  Optional<StoryRelationship> findByIdAndProjectId(UUID id, UUID projectId);
}
