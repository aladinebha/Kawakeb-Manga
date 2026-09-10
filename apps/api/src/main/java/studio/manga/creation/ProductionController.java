package studio.manga.creation;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/chapters/{chapterId}/production")
public class ProductionController {

  private final MangaPageRepository pageRepository;
  private final MangaAssetRepository assetRepository;
  private final ProjectRepository projects;
  private final CurrentUser user;

  public ProductionController(
      MangaPageRepository pageRepository,
      MangaAssetRepository assetRepository,
      ProjectRepository projects,
      CurrentUser user) {
    this.pageRepository = pageRepository;
    this.assetRepository = assetRepository;
    this.projects = projects;
    this.user = user;
  }

  // --- Pages ---

  @GetMapping("/pages")
  public ResponseEntity<List<MangaPage>> getPages(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId) {
    owned(projectId);
    return ResponseEntity.ok(pageRepository.findByChapterIdOrderByPageNumberAsc(chapterId));
  }

  @PostMapping("/pages")
  public ResponseEntity<MangaPage> createPage(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId,
      @RequestBody PageRequest request) {
    owned(projectId);
    MangaPage page = new MangaPage(chapterId, request.pageNumber(), request.layoutType());
    return ResponseEntity.ok(pageRepository.save(page));
  }

  @DeleteMapping("/pages/{id}")
  public ResponseEntity<Void> deletePage(
      @PathVariable UUID projectId,
      @PathVariable UUID id) {
    owned(projectId);
    pageRepository.findById(id).ifPresent(pageRepository::delete);
    return ResponseEntity.ok().build();
  }

  // --- Assets ---

  @GetMapping("/pages/{pageId}/assets")
  public ResponseEntity<List<MangaAsset>> getAssets(
      @PathVariable UUID projectId,
      @PathVariable UUID pageId) {
    owned(projectId);
    return ResponseEntity.ok(assetRepository.findByPageIdOrderByZIndexAsc(pageId));
  }

  @PostMapping("/pages/{pageId}/assets")
  public ResponseEntity<MangaAsset> createAsset(
      @PathVariable UUID projectId,
      @PathVariable UUID pageId,
      @RequestBody AssetRequest request) {
    owned(projectId);
    MangaAsset asset = new MangaAsset(
        pageId,
        request.assetType(),
        request.fileUrl(),
        request.posX(),
        request.posY(),
        request.width(),
        request.height(),
        request.zIndex()
    );
    return ResponseEntity.ok(assetRepository.save(asset));
  }

  @PutMapping("/assets/{id}")
  public ResponseEntity<MangaAsset> updateAsset(
      @PathVariable UUID projectId,
      @PathVariable UUID id,
      @RequestBody AssetRequest request) {
    owned(projectId);
    MangaAsset asset = assetRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Asset not found"));
    asset.assetType = request.assetType();
    asset.fileUrl = request.fileUrl();
    asset.posX = request.posX();
    asset.posY = request.posY();
    asset.width = request.width();
    asset.height = request.height();
    asset.zIndex = request.zIndex();
    return ResponseEntity.ok(assetRepository.save(asset));
  }

  @DeleteMapping("/assets/{id}")
  public ResponseEntity<Void> deleteAsset(
      @PathVariable UUID projectId,
      @PathVariable UUID id) {
    owned(projectId);
    assetRepository.findById(id).ifPresent(assetRepository::delete);
    return ResponseEntity.ok().build();
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  public record PageRequest(int pageNumber, String layoutType) {}

  public record AssetRequest(
      String assetType,
      String fileUrl,
      double posX,
      double posY,
      double width,
      double height,
      int zIndex) {}
}
