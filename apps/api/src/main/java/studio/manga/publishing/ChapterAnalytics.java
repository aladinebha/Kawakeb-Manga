package studio.manga.publishing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chapter_analytics")
public class ChapterAnalytics {
  @Id
  @Column(name = "chapter_id")
  public UUID chapterId;

  @Column(nullable = false)
  public int views = 0;

  @Column(nullable = false)
  public int likes = 0;

  @Column(name = "comments_count", nullable = false)
  public int commentsCount = 0;

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected ChapterAnalytics() {}

  public ChapterAnalytics(UUID chapterId) {
    this.chapterId = chapterId;
  }
}
