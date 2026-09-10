package studio.manga.timeline;

import java.time.Instant;
import java.util.*;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import studio.manga.entities.StoryEntityRepository;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.ProjectRepository;

@RestController
@RequestMapping("/api/projects/{projectId}/events")
public class TimelineEventController {
  private static final Set<String> STATUSES =
      Set.of("CANON", "DRAFT", "IDEA", "AI_SUGGESTION", "UNKNOWN", "CONTRADICTED");

  private final TimelineEventRepository events;
  private final StoryEntityRepository entities;
  private final ProjectRepository projects;
  private final CurrentUser user;

  TimelineEventController(
      TimelineEventRepository events,
      StoryEntityRepository entities,
      ProjectRepository projects,
      CurrentUser user) {
    this.events = events;
    this.entities = entities;
    this.projects = projects;
    this.user = user;
  }

  @GetMapping
  public List<TimelineEvent> list(@PathVariable UUID projectId) {
    owned(projectId);
    return events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId);
  }

  @GetMapping("/{eventId}")
  public TimelineEvent get(@PathVariable UUID projectId, @PathVariable UUID eventId) {
    owned(projectId);
    return events
        .findByIdAndProjectId(eventId, projectId)
        .orElseThrow(() -> new NoSuchElementException("Timeline event not found"));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public TimelineEvent create(
      @PathVariable UUID projectId, @RequestBody EventInput input) {
    owned(projectId);
    String title = required(input.title(), "Event title");
    String status = status(input.status());
    int orderIndex = input.orderIndex() != null && input.orderIndex() > 0
        ? input.orderIndex()
        : events.countByProjectId(projectId) + 1;

    Set<UUID> validatedEntityIds = validateEntities(projectId, input.entityIds());

    TimelineEvent event =
        new TimelineEvent(
            projectId,
            title,
            input.description(),
            input.storyTime(),
            orderIndex,
            input.sourceDocumentId(),
            status,
            validatedEntityIds);
    return events.save(event);
  }

  @PutMapping("/{eventId}")
  @Transactional
  public TimelineEvent update(
      @PathVariable UUID projectId,
      @PathVariable UUID eventId,
      @RequestBody EventInput input) {
    owned(projectId);
    TimelineEvent event =
        events
            .findByIdAndProjectId(eventId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Timeline event not found"));

    event.title = required(input.title(), "Event title");
    event.description = input.description() == null ? "" : input.description();
    event.storyTime = input.storyTime() == null ? "" : input.storyTime();
    if (input.orderIndex() != null) {
      event.orderIndex = input.orderIndex();
    }
    event.sourceDocumentId = input.sourceDocumentId();
    event.status = status(input.status());

    if (input.entityIds() != null) {
      Set<UUID> validated = validateEntities(projectId, input.entityIds());
      event.entityIds.clear();
      event.entityIds.addAll(validated);
    }

    event.updatedAt = Instant.now();
    return events.save(event);
  }

  @DeleteMapping("/{eventId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID projectId, @PathVariable UUID eventId) {
    owned(projectId);
    TimelineEvent event =
        events
            .findByIdAndProjectId(eventId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Timeline event not found"));
    events.delete(event);
  }

  @PostMapping("/reorder")
  @Transactional
  public List<TimelineEvent> reorder(
      @PathVariable UUID projectId, @RequestBody List<UUID> orderedEventIds) {
    owned(projectId);
    List<TimelineEvent> projectEvents =
        events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId);
    Map<UUID, TimelineEvent> map = new HashMap<>();
    for (TimelineEvent ev : projectEvents) {
      map.put(ev.id, ev);
    }

    int index = 1;
    for (UUID id : orderedEventIds) {
      TimelineEvent ev = map.get(id);
      if (ev != null) {
        ev.orderIndex = index++;
        ev.updatedAt = Instant.now();
        events.save(ev);
      }
    }
    return events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId);
  }

  private Set<UUID> validateEntities(UUID projectId, Set<UUID> entityIds) {
    if (entityIds == null || entityIds.isEmpty()) {
      return Collections.emptySet();
    }
    Set<UUID> result = new HashSet<>();
    for (UUID eid : entityIds) {
      entities
          .findByIdAndProjectId(eid, projectId)
          .orElseThrow(() -> new IllegalArgumentException("Linked story entry " + eid + " not found"));
      result.add(eid);
    }
    return result;
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

  public record EventInput(
      String title,
      String description,
      String storyTime,
      Integer orderIndex,
      UUID sourceDocumentId,
      String status,
      Set<UUID> entityIds) {}
}
