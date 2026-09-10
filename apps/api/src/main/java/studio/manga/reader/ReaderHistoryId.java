package studio.manga.reader;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ReaderHistoryId implements Serializable {
  public UUID userId;
  public UUID chapterId;

  public ReaderHistoryId() {}

  public ReaderHistoryId(UUID userId, UUID chapterId) {
    this.userId = userId;
    this.chapterId = chapterId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ReaderHistoryId that = (ReaderHistoryId) o;
    return Objects.equals(userId, that.userId) && Objects.equals(chapterId, that.chapterId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, chapterId);
  }
}
