package studio.manga.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "story_entity")
public class StoryEntity {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "project_id", nullable = false)
  public UUID projectId;

  @Column(name = "source_document_id")
  public UUID sourceDocumentId;

  @Column(name = "source_excerpt", nullable = false)
  public String sourceExcerpt = "";

  @Column(nullable = false)
  public String type;

  @Column(nullable = false)
  public String name;

  @Column(nullable = false)
  public String description = "";

  @Column(nullable = false)
  public String attributes = "{}";

  @Column(nullable = false)
  public String status = "DRAFT";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected StoryEntity() {}

  public StoryEntity(
      UUID projectId,
      String type,
      String name,
      String description,
      String status,
      UUID sourceDocumentId,
      String sourceExcerpt,
      String attributes) {
    this.projectId = projectId;
    this.type = type;
    this.name = name;
    this.description = description == null ? "" : description;
    this.status = status;
    this.sourceDocumentId = sourceDocumentId;
    this.sourceExcerpt = sourceExcerpt == null ? "" : sourceExcerpt;
    this.attributes = (attributes == null || attributes.isBlank()) ? "{}" : attributes;
  }
}
