package studio.manga.reader;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ReaderBookmarkRepository extends CrudRepository<ReaderBookmark, ReaderBookmarkId> {
  List<ReaderBookmark> findByUserId(UUID userId);
}
