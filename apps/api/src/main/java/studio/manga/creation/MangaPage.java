package studio.manga.creation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manga_page")
public class MangaPage {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "chapter_id", nullable = false)
  public UUID chapterId;

  @Column(name = "page_number", nullable = false)
  public int pageNumber;

  @Column(name = "layout_type", nullable = false)
  public String layoutType = "SINGLE";

  @Column(nullable = false)
  public String status = "DRAFT";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected MangaPage() {}

  public MangaPage(UUID chapterId, int pageNumber, String layoutType) {
    this.chapterId = chapterId;
    this.pageNumber = pageNumber;
    this.layoutType = layoutType;
  }
}
