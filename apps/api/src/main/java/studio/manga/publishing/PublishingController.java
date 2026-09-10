package studio.manga.publishing;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api")
public class PublishingController {

  private final CreatorProfileRepository profiles;
  private final ProjectPublishingRepository projectPublishing;
  private final ChapterReleaseRepository releases;
  private final ChapterAnalyticsRepository analytics;
  private final ProjectRepository projects;
  private final CurrentUser user;

  public PublishingController(
      CreatorProfileRepository profiles,
      ProjectPublishingRepository projectPublishing,
      ChapterReleaseRepository releases,
      ChapterAnalyticsRepository analytics,
      ProjectRepository projects,
      CurrentUser user) {
    this.profiles = profiles;
    this.projectPublishing = projectPublishing;
    this.releases = releases;
    this.analytics = analytics;
    this.projects = projects;
    this.user = user;
  }

  // --- Creator Profile ---

  @GetMapping("/publishing/profile")
  public ResponseEntity<CreatorProfile> getProfile() {
    return ResponseEntity.ok(
        profiles.findById(user.id())
            .orElseGet(() -> new CreatorProfile(user.id(), "Anonymous Creator", "", "")));
  }

  @PutMapping("/publishing/profile")
  public ResponseEntity<CreatorProfile> updateProfile(@RequestBody ProfileRequest request) {
    CreatorProfile profile = profiles.findById(user.id())
        .orElseGet(() -> new CreatorProfile(user.id(), request.penName(), request.bio(), request.avatarUrl()));
    profile.penName = request.penName();
    profile.bio = request.bio();
    profile.avatarUrl = request.avatarUrl();
    profile.updatedAt = Instant.now();
    return ResponseEntity.ok(profiles.save(profile));
  }

  // --- Project Publishing Settings ---

  @GetMapping("/projects/{projectId}/publishing")
  public ResponseEntity<ProjectPublishing> getProjectPublishing(@PathVariable UUID projectId) {
    owned(projectId);
    return ResponseEntity.ok(
        projectPublishing.findById(projectId)
            .orElseGet(() -> new ProjectPublishing(projectId)));
  }

  @PutMapping("/projects/{projectId}/publishing")
  public ResponseEntity<ProjectPublishing> updateProjectPublishing(
      @PathVariable UUID projectId,
      @RequestBody ProjectPublishingRequest request) {
    owned(projectId);
    ProjectPublishing settings = projectPublishing.findById(projectId)
        .orElseGet(() -> new ProjectPublishing(projectId));
    settings.visibility = request.visibility();
    settings.tags = request.tags();
    settings.updatedAt = Instant.now();
    return ResponseEntity.ok(projectPublishing.save(settings));
  }

  // --- Chapter Release & Analytics ---

  @GetMapping("/projects/{projectId}/chapters/{chapterId}/release")
  public ResponseEntity<ChapterRelease> getChapterRelease(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId) {
    owned(projectId);
    return ResponseEntity.ok(
        releases.findById(chapterId)
            .orElseGet(() -> new ChapterRelease(chapterId)));
  }

  @PostMapping("/projects/{projectId}/chapters/{chapterId}/release/publish")
  public ResponseEntity<ChapterRelease> publishChapter(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId) {
    owned(projectId);
    ChapterRelease release = releases.findById(chapterId)
        .orElseGet(() -> new ChapterRelease(chapterId));
    release.status = "PUBLISHED";
    release.publishedAt = Instant.now();
    release.updatedAt = Instant.now();
    
    // Initialize analytics if publishing for the first time
    if (!analytics.existsById(chapterId)) {
        analytics.save(new ChapterAnalytics(chapterId));
    }
    
    return ResponseEntity.ok(releases.save(release));
  }

  @GetMapping("/projects/{projectId}/chapters/{chapterId}/analytics")
  public ResponseEntity<ChapterAnalytics> getChapterAnalytics(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId) {
    owned(projectId);
    return ResponseEntity.ok(
        analytics.findById(chapterId)
            .orElseGet(() -> new ChapterAnalytics(chapterId)));
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  public record ProfileRequest(String penName, String bio, String avatarUrl) {}
  public record ProjectPublishingRequest(String visibility, String tags) {}
}
