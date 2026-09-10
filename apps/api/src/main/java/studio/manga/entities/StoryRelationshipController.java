package studio.manga.entities;

import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/relationships")
public class StoryRelationshipController {
  private static final Set<String> STATUSES =
      Set.of("CANON", "DRAFT", "IDEA", "AI_SUGGESTION", "UNKNOWN", "CONTRADICTED");

  private final StoryRelationshipRepository relationships;
  private final StoryEntityRepository entities;
  private final ProjectRepository projects;
  private final CurrentUser user;

  StoryRelationshipController(
      StoryRelationshipRepository relationships,
      StoryEntityRepository entities,
      ProjectRepository projects,
      CurrentUser user) {
    this.relationships = relationships;
    this.entities = entities;
    this.projects = projects;
    this.user = user;
  }

  @GetMapping
  public List<StoryRelationship> list(@PathVariable UUID projectId) {
    owned(projectId);
    return relationships.findByProjectIdOrderByUpdatedAtDesc(projectId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public StoryRelationship create(
      @PathVariable UUID projectId, @RequestBody RelationshipInput input) {
    owned(projectId);
    if (input.sourceEntityId() == null
        || input.targetEntityId() == null
        || Objects.equals(input.sourceEntityId(), input.targetEntityId())) {
      throw new IllegalArgumentException("Choose two different story entries.");
    }
    entities
        .findByIdAndProjectId(input.sourceEntityId(), projectId)
        .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
    entities
        .findByIdAndProjectId(input.targetEntityId(), projectId)
        .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
    String type = required(input.relationshipType(), "Relationship type");
    String status = status(input.status());
    return relationships.save(
        new StoryRelationship(
            projectId,
            input.sourceEntityId(),
            input.targetEntityId(),
            type,
            input.description(),
            status,
            input.sourceDocumentId()));
  }

  @PutMapping("/{relationshipId}")
  public StoryRelationship update(
      @PathVariable UUID projectId,
      @PathVariable UUID relationshipId,
      @RequestBody RelationshipInput input) {
    owned(projectId);
    StoryRelationship rel =
        relationships
            .findByIdAndProjectId(relationshipId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Relationship not found"));
    if (input.sourceEntityId() != null
        && input.targetEntityId() != null
        && !Objects.equals(input.sourceEntityId(), input.targetEntityId())) {
      entities
          .findByIdAndProjectId(input.sourceEntityId(), projectId)
          .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
      entities
          .findByIdAndProjectId(input.targetEntityId(), projectId)
          .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
      rel.sourceEntityId = input.sourceEntityId();
      rel.targetEntityId = input.targetEntityId();
    }
    if (input.relationshipType() != null && !input.relationshipType().isBlank()) {
      rel.relationshipType = required(input.relationshipType(), "Relationship type");
    }
    if (input.description() != null) {
      rel.description = input.description();
    }
    if (input.status() != null) {
      rel.status = status(input.status());
    }
    rel.sourceDocumentId = input.sourceDocumentId();
    rel.updatedAt = Instant.now();
    return relationships.save(rel);
  }

  @DeleteMapping("/{relationshipId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID projectId, @PathVariable UUID relationshipId) {
    owned(projectId);
    StoryRelationship rel =
        relationships
            .findByIdAndProjectId(relationshipId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Relationship not found"));
    relationships.delete(rel);
  }

  private void owned(UUID id) {
    projects
        .findByIdAndOwnerId(id, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  private String status(String value) {
    String normalized = Optional.ofNullable(value).orElse("DRAFT").toUpperCase();
    if (!STATUSES.contains(normalized)) {
      throw new IllegalArgumentException("Unsupported canon status");
    }
    return normalized;
  }

  private String required(String value, String label) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  public record RelationshipInput(
      UUID sourceEntityId,
      UUID targetEntityId,
      String relationshipType,
      String description,
      String status,
      UUID sourceDocumentId) {}
}
