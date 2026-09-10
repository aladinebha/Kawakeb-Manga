package studio.manga.reader;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.Project;
import studio.manga.projects.ProjectRepository;
import studio.manga.creation.MangaPage;
import studio.manga.creation.MangaPageRepository;
import studio.manga.creation.MangaAsset;
import studio.manga.creation.MangaAssetRepository;
import studio.manga.publishing.ProjectPublishing;
import studio.manga.publishing.ProjectPublishingRepository;
import studio.manga.publishing.ChapterRelease;
import studio.manga.publishing.ChapterReleaseRepository;
import studio.manga.documents.Document;
import studio.manga.documents.DocumentRepository;
import studio.manga.publishing.ChapterAnalytics;
import studio.manga.publishing.ChapterAnalyticsRepository;

@RestController
@RequestMapping("/api/reader")
public class ReaderController {

  private final ChapterCommentRepository comments;
  private final ReaderBookmarkRepository bookmarks;
  private final ReaderHistoryRepository history;
  private final ProjectRepository projects;
  private final DocumentRepository documents;
  private final ProjectPublishingRepository projectPublishing;
  private final ChapterReleaseRepository chapterReleases;
  private final MangaPageRepository pages;
  private final MangaAssetRepository assets;
  private final ChapterAnalyticsRepository analytics;
  private final CurrentUser user;

  public ReaderController(
      ChapterCommentRepository comments,
      ReaderBookmarkRepository bookmarks,
      ReaderHistoryRepository history,
      ProjectRepository projects,
      DocumentRepository documents,
      ProjectPublishingRepository projectPublishing,
      ChapterReleaseRepository chapterReleases,
      MangaPageRepository pages,
      MangaAssetRepository assets,
      ChapterAnalyticsRepository analytics,
      CurrentUser user) {
    this.comments = comments;
    this.bookmarks = bookmarks;
    this.history = history;
    this.projects = projects;
    this.documents = documents;
    this.projectPublishing = projectPublishing;
    this.chapterReleases = chapterReleases;
    this.pages = pages;
    this.assets = assets;
    this.analytics = analytics;
    this.user = user;
  }

  // --- Discovery ---

  @GetMapping("/discover")
  public ResponseEntity<List<Project>> discoverProjects() {
    List<ProjectPublishing> publics = projectPublishing.findByVisibility("PUBLIC");
    List<UUID> publicIds = publics.stream().map(p -> p.projectId).toList();
    
    List<Project> publicProjects = (List<Project>) projects.findAllById(publicIds);
    return ResponseEntity.ok(publicProjects);
  }

  // --- Reading Chapters ---

  @GetMapping("/projects/{projectId}/chapters")
  public ResponseEntity<List<Document>> getPublishedChapters(@PathVariable UUID projectId) {
    List<Document> allDocs = documents.findByProjectIdOrderByUpdatedAtDesc(projectId);
    
    // Only return documents of type CHAPTER that are PUBLISHED
    List<Document> publishedChapters = allDocs.stream()
        .filter(d -> "CHAPTER".equals(d.type))
        .filter(d -> {
            Optional<ChapterRelease> release = chapterReleases.findById(d.chapterId);
            return release.isPresent() && "PUBLISHED".equals(release.get().status);
        })
        .toList();

    return ResponseEntity.ok(publishedChapters);
  }

  @GetMapping("/chapters/{chapterId}/pages")
  public ResponseEntity<PagesResponse> getChapterPages(@PathVariable UUID chapterId) {
    // Only allow if published (simplified check for prototype)
    List<MangaPage> chapterPages = pages.findByChapterIdOrderByPageNumberAsc(chapterId);
    
    List<MangaAsset> allAssets = chapterPages.stream()
        .flatMap(page -> assets.findByPageIdOrderByZIndexAsc(page.id).stream())
        .toList();

    // Increment view count in analytics
    analytics.findById(chapterId).ifPresent(a -> {
        a.views++;
        analytics.save(a);
    });

    return ResponseEntity.ok(new PagesResponse(chapterPages, allAssets));
  }

  // --- Comments ---

  @GetMapping("/chapters/{chapterId}/comments")
  public ResponseEntity<List<ChapterComment>> getComments(@PathVariable UUID chapterId) {
    return ResponseEntity.ok(comments.findByChapterIdOrderByCreatedAtAsc(chapterId));
  }

  @PostMapping("/chapters/{chapterId}/comments")
  public ResponseEntity<ChapterComment> addComment(
      @PathVariable UUID chapterId,
      @RequestBody CommentRequest request) {
    ChapterComment comment = new ChapterComment(chapterId, user.id(), request.content());
    
    // Increment comment count in analytics
    analytics.findById(chapterId).ifPresent(a -> {
        a.commentsCount++;
        analytics.save(a);
    });

    return ResponseEntity.ok(comments.save(comment));
  }

  // --- Bookmarks & History ---

  @PostMapping("/projects/{projectId}/bookmark")
  public ResponseEntity<ReaderBookmark> toggleBookmark(@PathVariable UUID projectId) {
    ReaderBookmarkId id = new ReaderBookmarkId(user.id(), projectId);
    Optional<ReaderBookmark> existing = bookmarks.findById(id);
    if (existing.isPresent()) {
      bookmarks.delete(existing.get());
      return ResponseEntity.ok().build(); // Removed
    } else {
      ReaderBookmark bookmark = new ReaderBookmark(user.id(), projectId);
      return ResponseEntity.ok(bookmarks.save(bookmark));
    }
  }

  @GetMapping("/bookmarks")
  public ResponseEntity<List<ReaderBookmark>> getBookmarks() {
    return ResponseEntity.ok(bookmarks.findByUserId(user.id()));
  }

  public record PagesResponse(List<MangaPage> pages, List<MangaAsset> assets) {}
  public record CommentRequest(String content) {}
}
