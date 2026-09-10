package studio.manga.preproduction;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface VisualReferenceRepository extends CrudRepository<VisualReference, UUID> {
  List<VisualReference> findByEntityId(UUID entityId);
  List<VisualReference> findByProjectId(UUID projectId);
}
