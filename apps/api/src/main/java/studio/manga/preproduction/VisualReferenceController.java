package studio.manga.preproduction;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/visual-references")
public class VisualReferenceController {

  private final VisualReferenceRepository visualReferenceRepository;
  private final ProjectRepository projects;
  private final CurrentUser user;

  public VisualReferenceController(
      VisualReferenceRepository visualReferenceRepository,
      ProjectRepository projects,
      CurrentUser user) {
    this.visualReferenceRepository = visualReferenceRepository;
    this.projects = projects;
    this.user = user;
  }

  @GetMapping
  public ResponseEntity<List<VisualReference>> getVisualReferencesByEntity(
      @PathVariable UUID projectId,
      @RequestParam UUID entityId) {
    owned(projectId);
    List<VisualReference> refs = visualReferenceRepository.findByEntityId(entityId);
    return ResponseEntity.ok(refs);
  }

  @PostMapping
  public ResponseEntity<VisualReference> createVisualReference(
      @PathVariable UUID projectId,
      @RequestBody VisualReferenceRequest request) {
    owned(projectId);
    VisualReference ref = new VisualReference(
        projectId,
        request.entityId(),
        request.filePath(),
        request.caption(),
        request.type(),
        request.status()
    );
    return ResponseEntity.ok(visualReferenceRepository.save(ref));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteVisualReference(
      @PathVariable UUID projectId,
      @PathVariable UUID id) {
    owned(projectId);
    visualReferenceRepository.findById(id).ifPresent(ref -> {
      if (ref.projectId.equals(projectId)) {
        visualReferenceRepository.delete(ref);
      }
    });
    return ResponseEntity.ok().build();
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  public record VisualReferenceRequest(
      UUID entityId,
      String filePath,
      String caption,
      String type,
      String status) {}
}
