package studio.manga.timeline;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {
  List<TimelineEvent> findByProjectIdOrderByOrderIndexAscCreatedAtAsc(UUID projectId);
  Optional<TimelineEvent> findByIdAndProjectId(UUID id, UUID projectId);
  int countByProjectId(UUID projectId);
}
