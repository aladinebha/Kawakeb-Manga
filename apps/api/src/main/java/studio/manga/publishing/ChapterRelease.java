package studio.manga.publishing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chapter_release")
public class ChapterRelease {
  @Id
  @Column(name = "chapter_id")
  public UUID chapterId;

  @Column(nullable = false)
  public String status = "DRAFT";

  @Column(name = "scheduled_for")
  public Instant scheduledFor;

  @Column(name = "published_at")
  public Instant publishedAt;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected ChapterRelease() {}

  public ChapterRelease(UUID chapterId) {
    this.chapterId = chapterId;
  }
}
