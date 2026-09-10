package studio.manga.entities;

import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/entities")
public class StoryEntityController {
  private static final Set<String> TYPES =
      Set.of("CHARACTER", "LOCATION", "ORGANIZATION", "OBJECT", "CONCEPT");
  private static final Set<String> STATUSES =
      Set.of("CANON", "DRAFT", "IDEA", "AI_SUGGESTION", "UNKNOWN", "CONTRADICTED");

  private final StoryEntityRepository entities;
  private final StoryRelationshipRepository relationships;
  private final ProjectRepository projects;
  private final CurrentUser user;

  StoryEntityController(
      StoryEntityRepository entities,
      StoryRelationshipRepository relationships,
      ProjectRepository projects,
      CurrentUser user) {
    this.entities = entities;
    this.relationships = relationships;
    this.projects = projects;
    this.user = user;
  }

  @GetMapping
  public List<StoryEntity> list(@PathVariable UUID projectId) {
    owned(projectId);
    return entities.findByProjectIdOrderByUpdatedAtDesc(projectId);
  }

  @GetMapping("/{entityId}")
  public StoryEntity get(@PathVariable UUID projectId, @PathVariable UUID entityId) {
    owned(projectId);
    return entities
        .findByIdAndProjectId(entityId, projectId)
        .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public StoryEntity create(@PathVariable UUID projectId, @RequestBody EntityInput input) {
    owned(projectId);
    return entities.save(
        new StoryEntity(
            projectId,
            type(input.type()),
            required(input.name(), "Name"),
            input.description(),
            status(input.status()),
            input.sourceDocumentId(),
            input.sourceExcerpt(),
            input.attributes()));
  }

  @PutMapping("/{entityId}")
  public StoryEntity update(
      @PathVariable UUID projectId,
      @PathVariable UUID entityId,
      @RequestBody EntityInput input) {
    owned(projectId);
    StoryEntity entity =
        entities
            .findByIdAndProjectId(entityId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
    entity.type = type(input.type());
    entity.name = required(input.name(), "Name");
    entity.description = input.description() == null ? "" : input.description();
    entity.status = status(input.status());
    entity.sourceDocumentId = input.sourceDocumentId();
    entity.sourceExcerpt = input.sourceExcerpt() == null ? "" : input.sourceExcerpt();
    if (input.attributes() != null && !input.attributes().isBlank()) {
      entity.attributes = input.attributes();
    }
    entity.updatedAt = Instant.now();
    return entities.save(entity);
  }

  @DeleteMapping("/{entityId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID projectId, @PathVariable UUID entityId) {
    owned(projectId);
    StoryEntity entity =
        entities
            .findByIdAndProjectId(entityId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Story entry not found"));
    entities.delete(entity);
  }

  private void owned(UUID projectId) {
    projects
        .findByIdAndOwnerId(projectId, user.id())
        .orElseThrow(() -> new NoSuchElementException("Project not found"));
  }

  private String type(String value) {
    String normalized = Optional.ofNullable(value).orElse("CONCEPT").toUpperCase();
    if (!TYPES.contains(normalized)) throw new IllegalArgumentException("Unsupported story entry type");
    return normalized;
  }

  private String status(String value) {
    String normalized = Optional.ofNullable(value).orElse("DRAFT").toUpperCase();
    if (!STATUSES.contains(normalized)) throw new IllegalArgumentException("Unsupported canon status");
    return normalized;
  }

  private String required(String value, String label) {
    if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required.");
    return value.trim();
  }

  public record EntityInput(
      String type,
      String name,
      String description,
      String status,
      UUID sourceDocumentId,
      String sourceExcerpt,
      String attributes) {}
}
