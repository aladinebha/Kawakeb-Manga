package studio.manga.publishing;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ProjectPublishingRepository extends CrudRepository<ProjectPublishing, UUID> {
}
