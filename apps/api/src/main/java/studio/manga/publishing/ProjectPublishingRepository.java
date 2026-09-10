package studio.manga.publishing;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ProjectPublishingRepository extends CrudRepository<ProjectPublishing, UUID> {
  List<ProjectPublishing> findByVisibility(String visibility);
}
