package studio.manga.preproduction;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface SceneRepository extends CrudRepository<Scene, UUID> {
  List<Scene> findByChapterIdOrderByOrderIndexAsc(UUID chapterId);
}
