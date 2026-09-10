package studio.manga.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "story_relationship")
public class StoryRelationship {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "project_id", nullable = false)
  public UUID projectId;

  @Column(name = "source_entity_id", nullable = false)
  public UUID sourceEntityId;

  @Column(name = "target_entity_id", nullable = false)
  public UUID targetEntityId;

  @Column(name = "source_document_id")
  public UUID sourceDocumentId;

  @Column(name = "relationship_type", nullable = false)
  public String relationshipType;

  @Column(nullable = false)
  public String description = "";

  @Column(nullable = false)
  public String status = "DRAFT";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected StoryRelationship() {}

  public StoryRelationship(
      UUID projectId,
      UUID source,
      UUID target,
      String type,
      String description,
      String status,
      UUID sourceDocumentId) {
    this.projectId = projectId;
    this.sourceEntityId = source;
    this.targetEntityId = target;
    this.relationshipType = type;
    this.description = description == null ? "" : description;
    this.status = status;
    this.sourceDocumentId = sourceDocumentId;
  }
}
