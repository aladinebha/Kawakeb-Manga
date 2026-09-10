package studio.manga.memory;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "memory_proposal")
public class MemoryProposal {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "project_id", nullable = false)
  public UUID projectId;

  @Column(name = "source_document_id", nullable = false)
  public UUID sourceDocumentId;

  @Column(name = "target_type", nullable = false)
  public String targetType; // ENTITY, RELATIONSHIP, EVENT, ATTRIBUTE

  @Column(nullable = false)
  public String name;

  @Column(name = "suggested_type", nullable = false)
  public String suggestedType; // e.g. CHARACTER, LOCATION, ORGANIZATION

  @Column(name = "suggested_details", nullable = false)
  public String suggestedDetails = "";

  @Column(name = "source_excerpt", nullable = false)
  public String sourceExcerpt = "";

  @Column(nullable = false)
  public BigDecimal confidence = new BigDecimal("0.85");

  @Column(nullable = false)
  public String status = "PENDING"; // PENDING, APPROVED, REJECTED

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected MemoryProposal() {}

  public MemoryProposal(
      UUID projectId,
      UUID sourceDocumentId,
      String targetType,
      String name,
      String suggestedType,
      String suggestedDetails,
      String sourceExcerpt,
      BigDecimal confidence) {
    this.projectId = projectId;
    this.sourceDocumentId = sourceDocumentId;
    this.targetType = targetType;
    this.name = name;
    this.suggestedType = suggestedType;
    this.suggestedDetails = suggestedDetails == null ? "" : suggestedDetails;
    this.sourceExcerpt = sourceExcerpt == null ? "" : sourceExcerpt;
    this.confidence = confidence == null ? new BigDecimal("0.85") : confidence;
    this.status = "PENDING";
  }
}
