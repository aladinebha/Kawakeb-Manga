package studio.manga.memory;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "semantic_memory_chunk")
public class SemanticMemoryChunk {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "project_id", nullable = false)
  public UUID projectId;

  @Column(name = "source_document_id")
  public UUID sourceDocumentId;

  @Column(name = "entity_id")
  public UUID entityId;

  @Column(name = "chunk_type", nullable = false)
  public String chunkType; // SCENE, CHAPTER_SECTION, CHARACTER_LORE, TIMELINE_SUMMARY

  @Column(nullable = false)
  public String title;

  @Column(nullable = false)
  public String content;

  @Column(nullable = false)
  public String tags = "";

  @Column(nullable = false)
  public String authority = "CANON";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected SemanticMemoryChunk() {}

  public SemanticMemoryChunk(
      UUID projectId,
      UUID sourceDocumentId,
      UUID entityId,
      String chunkType,
      String title,
      String content,
      String tags,
      String authority) {
    this.projectId = projectId;
    this.sourceDocumentId = sourceDocumentId;
    this.entityId = entityId;
    this.chunkType = chunkType;
    this.title = title;
    this.content = content == null ? "" : content;
    this.tags = tags == null ? "" : tags;
    this.authority = authority == null ? "CANON" : authority;
  }
}
