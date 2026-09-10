package studio.manga.publishing;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface CreatorProfileRepository extends CrudRepository<CreatorProfile, UUID> {
}
