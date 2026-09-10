package studio.manga.reader;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reader_history")
@IdClass(ReaderHistoryId.class)
public class ReaderHistory {
  @Id
  @Column(name = "user_id")
  public UUID userId;

  @Id
  @Column(name = "chapter_id")
  public UUID chapterId;

  @Column(name = "last_read_page", nullable = false)
  public int lastReadPage = 1;

  @Column(name = "read_at", nullable = false)
  public Instant readAt = Instant.now();

  protected ReaderHistory() {}

  public ReaderHistory(UUID userId, UUID chapterId) {
    this.userId = userId;
    this.chapterId = chapterId;
  }
}
