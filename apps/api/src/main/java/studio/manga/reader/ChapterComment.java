package studio.manga.reader;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chapter_comment")
public class ChapterComment {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "chapter_id", nullable = false)
  public UUID chapterId;

  @Column(name = "user_id", nullable = false)
  public UUID userId;

  @Column(nullable = false)
  public String content;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  protected ChapterComment() {}

  public ChapterComment(UUID chapterId, UUID userId, String content) {
    this.chapterId = chapterId;
    this.userId = userId;
    this.content = content;
  }
}
