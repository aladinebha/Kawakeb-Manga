package studio.manga.preproduction;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/chapters/{chapterId}/storyboard")
public class StoryboardController {

  private final SceneRepository sceneRepository;
  private final PanelProposalRepository panelProposalRepository;
  private final ProjectRepository projects;
  private final CurrentUser user;

  public StoryboardController(
      SceneRepository sceneRepository,
      PanelProposalRepository panelProposalRepository,
      ProjectRepository projects,
      CurrentUser user) {
    this.sceneRepository = sceneRepository;
    this.panelProposalRepository = panelProposalRepository;
    this.projects = projects;
    this.user = user;
  }

  // --- Scenes ---

  @GetMapping("/scenes")
  public ResponseEntity<List<Scene>> getScenes(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId) {
    owned(projectId);
    return ResponseEntity.ok(sceneRepository.findByChapterIdOrderByOrderIndexAsc(chapterId));
  }

  @PostMapping("/scenes")
  public ResponseEntity<Scene> createScene(
      @PathVariable UUID projectId,
      @PathVariable UUID chapterId,
      @RequestBody SceneRequest request) {
    owned(projectId);
    Scene scene = new Scene(chapterId, request.orderIndex(), request.description());
    return ResponseEntity.ok(sceneRepository.save(scene));
  }

  @DeleteMapping("/scenes/{id}")
  public ResponseEntity<Void> deleteScene(
      @PathVariable UUID projectId,
      @PathVariable UUID id) {
    owned(projectId);
    sceneRepository.findById(id).ifPresent(scene -> {
      sceneRepository.delete(scene);
    });
    return ResponseEntity.ok().build();
  }

  // --- Panel Proposals ---

  @GetMapping("/scenes/{sceneId}/panels")
  public ResponseEntity<List<PanelProposal>> getPanels(
      @PathVariable UUID projectId,
      @PathVariable UUID sceneId) {
    owned(projectId);
    return ResponseEntity.ok(panelProposalRepository.findBySceneIdOrderByOrderIndexAsc(sceneId));
  }

  @PostMapping("/scenes/{sceneId}/panels")
  public ResponseEntity<PanelProposal> createPanel(
      @PathVariable UUID projectId,
      @PathVariable UUID sceneId,
      @RequestBody PanelRequest request) {
    owned(projectId);
    PanelProposal panel = new PanelProposal(
        sceneId,
        request.orderIndex(),
        request.visualPrompt(),
        request.dialogue(),
        request.status()
    );
    return ResponseEntity.ok(panelProposalRepository.save(panel));
  }

  @DeleteMapping("/panels/{id}")
  public ResponseEntity<Void> deletePanel(
      @PathVariable UUID projectId,
      @PathVariable UUID id) {
    owned(projectId);
    panelProposalRepository.findById(id).ifPresent(panelProposalRepository::delete);
    return ResponseEntity.ok().build();
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  public record SceneRequest(int orderIndex, String description) {}

  public record PanelRequest(
      int orderIndex,
      String visualPrompt,
      String dialogue,
      String status) {}
}
