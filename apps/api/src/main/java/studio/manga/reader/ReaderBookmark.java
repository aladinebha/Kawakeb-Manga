package studio.manga.reader;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reader_bookmark")
@IdClass(ReaderBookmarkId.class)
public class ReaderBookmark {
  @Id
  @Column(name = "user_id")
  public UUID userId;

  @Id
  @Column(name = "project_id")
  public UUID projectId;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  protected ReaderBookmark() {}

  public ReaderBookmark(UUID userId, UUID projectId) {
    this.userId = userId;
    this.projectId = projectId;
  }
}
