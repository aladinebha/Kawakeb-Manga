package studio.manga.preproduction;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "scene")
public class Scene {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "chapter_id", nullable = false)
  public UUID chapterId;

  @Column(name = "order_index", nullable = false)
  public int orderIndex;

  @Column(nullable = false)
  public String description;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected Scene() {}

  public Scene(UUID chapterId, int orderIndex, String description) {
    this.chapterId = chapterId;
    this.orderIndex = orderIndex;
    this.description = description;
  }
}
