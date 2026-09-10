package studio.manga.creation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "manga_asset")
public class MangaAsset {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "page_id", nullable = false)
  public UUID pageId;

  @Column(name = "asset_type", nullable = false)
  public String assetType;

  @Column(name = "file_url", nullable = false)
  public String fileUrl;

  @Column(name = "pos_x", nullable = false)
  public double posX;

  @Column(name = "pos_y", nullable = false)
  public double posY;

  @Column(nullable = false)
  public double width;

  @Column(nullable = false)
  public double height;

  @Column(name = "z_index", nullable = false)
  public int zIndex;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected MangaAsset() {}

  public MangaAsset(
      UUID pageId,
      String assetType,
      String fileUrl,
      double posX,
      double posY,
      double width,
      double height,
      int zIndex) {
    this.pageId = pageId;
    this.assetType = assetType;
    this.fileUrl = fileUrl;
    this.posX = posX;
    this.posY = posY;
    this.width = width;
    this.height = height;
    this.zIndex = zIndex;
  }
}
