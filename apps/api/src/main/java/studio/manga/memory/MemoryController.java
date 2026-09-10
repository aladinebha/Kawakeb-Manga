package studio.manga.memory;

import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/memory")
public class MemoryController {
  private final MemoryService memoryService;
  private final MemoryProposalRepository proposals;
  private final SemanticMemoryRepository semanticChunks;
  private final ProjectRepository projects;
  private final CurrentUser user;

  public MemoryController(
      MemoryService memoryService,
      MemoryProposalRepository proposals,
      SemanticMemoryRepository semanticChunks,
      ProjectRepository projects,
      CurrentUser user) {
    this.memoryService = memoryService;
    this.proposals = proposals;
    this.semanticChunks = semanticChunks;
    this.projects = projects;
    this.user = user;
  }

  @GetMapping("/proposals")
  public List<MemoryProposal> listProposals(@PathVariable UUID projectId) {
    owned(projectId);
    return proposals.findByProjectIdOrderByCreatedAtDesc(projectId);
  }

  @PostMapping("/extract")
  @ResponseStatus(HttpStatus.CREATED)
  public List<MemoryProposal> extract(
      @PathVariable UUID projectId, @RequestBody ExtractInput input) {
    owned(projectId);
    if (input.documentId() == null) {
      throw new IllegalArgumentException("documentId is required for extraction");
    }
    return memoryService.extractFromDocument(projectId, input.documentId());
  }

  @PostMapping("/proposals/{proposalId}/approve")
  public MemoryProposal approve(
      @PathVariable UUID projectId, @PathVariable UUID proposalId) {
    owned(projectId);
    return memoryService.approveProposal(projectId, proposalId);
  }

  @PostMapping("/proposals/{proposalId}/reject")
  public MemoryProposal reject(
      @PathVariable UUID projectId, @PathVariable UUID proposalId) {
    owned(projectId);
    return memoryService.rejectProposal(projectId, proposalId);
  }

  @GetMapping("/chunks")
  public List<SemanticMemoryChunk> listChunks(@PathVariable UUID projectId) {
    owned(projectId);
    return semanticChunks.findByProjectIdOrderByCreatedAtDesc(projectId);
  }

  @PostMapping("/retrieve")
  public MemoryContext retrieve(
      @PathVariable UUID projectId, @RequestBody RetrieveInput input) {
    owned(projectId);
    return memoryService.retrieveContext(projectId, input.query(), input.selectedText());
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  public record ExtractInput(UUID documentId) {}

  public record RetrieveInput(String query, String selectedText) {}
}
