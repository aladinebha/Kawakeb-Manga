package studio.manga.creation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface MangaPageRepository extends CrudRepository<MangaPage, UUID> {
  List<MangaPage> findByChapterIdOrderByPageNumberAsc(UUID chapterId);
}
