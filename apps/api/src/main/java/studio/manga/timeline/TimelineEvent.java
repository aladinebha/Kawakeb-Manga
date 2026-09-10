package studio.manga.timeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "timeline_event")
public class TimelineEvent {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "project_id", nullable = false)
  public UUID projectId;

  @Column(nullable = false)
  public String title;

  @Column(nullable = false)
  public String description = "";

  @Column(name = "story_time", nullable = false)
  public String storyTime = "";

  @Column(name = "order_index", nullable = false)
  public int orderIndex = 0;

  @Column(name = "source_document_id")
  public UUID sourceDocumentId;

  @Column(nullable = false)
  public String status = "DRAFT";

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "timeline_event_entity",
      joinColumns = @JoinColumn(name = "event_id"))
  @Column(name = "entity_id")
  public Set<UUID> entityIds = new HashSet<>();

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected TimelineEvent() {}

  public TimelineEvent(
      UUID projectId,
      String title,
      String description,
      String storyTime,
      int orderIndex,
      UUID sourceDocumentId,
      String status,
      Set<UUID> entityIds) {
    this.projectId = projectId;
    this.title = title;
    this.description = description == null ? "" : description;
    this.storyTime = storyTime == null ? "" : storyTime;
    this.orderIndex = orderIndex;
    this.sourceDocumentId = sourceDocumentId;
    this.status = status;
    if (entityIds != null) {
      this.entityIds.addAll(entityIds);
    }
  }
}
