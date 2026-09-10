package studio.manga.reader;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ChapterCommentRepository extends CrudRepository<ChapterComment, UUID> {
  List<ChapterComment> findByChapterIdOrderByCreatedAtAsc(UUID chapterId);
}
