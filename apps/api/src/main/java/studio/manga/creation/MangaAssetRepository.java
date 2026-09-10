package studio.manga.creation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface MangaAssetRepository extends CrudRepository<MangaAsset, UUID> {
  List<MangaAsset> findByPageIdOrderByZIndexAsc(UUID pageId);
}
