package studio.manga.preproduction;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "visual_reference")
public class VisualReference {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "project_id", nullable = false)
  public UUID projectId;

  @Column(name = "entity_id", nullable = false)
  public UUID entityId;

  @Column(name = "file_path", nullable = false)
  public String filePath;

  @Column
  public String caption;

  @Column(nullable = false)
  public String type;

  @Column(nullable = false)
  public String status = "PROPOSED";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected VisualReference() {}

  public VisualReference(UUID projectId, UUID entityId, String filePath, String caption, String type, String status) {
    this.projectId = projectId;
    this.entityId = entityId;
    this.filePath = filePath;
    this.caption = caption;
    this.type = type;
    this.status = status;
  }
}
