package studio.manga.publishing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "project_publishing")
public class ProjectPublishing {
  @Id
  @Column(name = "project_id")
  public UUID projectId;

  @Column(nullable = false)
  public String visibility = "PRIVATE";

  @Column(nullable = false)
  public String tags = "";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected ProjectPublishing() {}

  public ProjectPublishing(UUID projectId) {
    this.projectId = projectId;
  }
}
