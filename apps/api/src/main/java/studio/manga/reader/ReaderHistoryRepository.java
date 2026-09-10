package studio.manga.reader;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface ReaderHistoryRepository extends CrudRepository<ReaderHistory, ReaderHistoryId> {
  List<ReaderHistory> findByUserId(UUID userId);
}
