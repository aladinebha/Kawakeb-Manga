package studio.manga.continuity;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/continuity")
public class ContinuityController {
  private final ContinuityService continuityService;
  private final ProjectRepository projects;
  private final CurrentUser user;

  public ContinuityController(
      ContinuityService continuityService,
      ProjectRepository projects,
      CurrentUser user) {
    this.continuityService = continuityService;
    this.projects = projects;
    this.user = user;
  }

  @GetMapping
  public List<ContinuityNotice> getNotices(@PathVariable UUID projectId) {
    owned(projectId);
    return continuityService.checkProject(projectId);
  }

  @PostMapping("/check")
  public List<ContinuityNotice> checkScene(
      @PathVariable UUID projectId, @RequestBody CheckInput input) {
    owned(projectId);
    return continuityService.checkText(projectId, input.text());
  }

  @PostMapping("/resolve")
  public List<ContinuityNotice> resolveNotice(
      @PathVariable UUID projectId, @RequestBody ResolutionInput input) {
    owned(projectId);

    if (ContinuityNotice.RES_DISMISS.equalsIgnoreCase(input.action())) {
      continuityService.dismiss(input.noticeId());
    } else if (ContinuityNotice.RES_MARK_CANON.equalsIgnoreCase(input.action())) {
      if (input.entityId() != null) {
        continuityService.resolveEntity(input.entityId(), "CANON");
      }
      if (input.relationshipId() != null) {
        continuityService.resolveRelationship(input.relationshipId(), "CANON");
      }
      continuityService.dismiss(input.noticeId());
    } else if (ContinuityNotice.RES_MARK_DRAFT.equalsIgnoreCase(input.action())) {
      if (input.entityId() != null) {
        continuityService.resolveEntity(input.entityId(), "DRAFT");
      }
      if (input.relationshipId() != null) {
        continuityService.resolveRelationship(input.relationshipId(), "DRAFT");
      }
      continuityService.dismiss(input.noticeId());
    }

    return continuityService.checkProject(projectId);
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  public record CheckInput(String text) {}

  public record ResolutionInput(
      String noticeId,
      String action,
      UUID entityId,
      UUID relationshipId,
      UUID eventId) {}
}
