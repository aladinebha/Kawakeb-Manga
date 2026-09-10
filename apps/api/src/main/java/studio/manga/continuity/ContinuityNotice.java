package studio.manga.continuity;

import java.util.List;
import java.util.UUID;

public record ContinuityNotice(
    String id,
    String type,
    String severity,
    String title,
    String description,
    UUID entityId,
    UUID relationshipId,
    UUID eventId,
    UUID documentId,
    List<String> suggestedResolutions) {

  public static final String TYPE_CONTRADICTION = "CONTRADICTION";
  public static final String TYPE_TIMELINE_ANOMALY = "TIMELINE_ANOMALY";
  public static final String TYPE_RELATIONSHIP_CONFLICT = "RELATIONSHIP_CONFLICT";
  public static final String TYPE_LORE_DISCREPANCY = "LORE_DISCREPANCY";

  public static final String SEVERITY_WARNING = "WARNING";
  public static final String SEVERITY_INFO = "INFO";

  public static final String RES_MARK_CANON = "MARK_CANON";
  public static final String RES_MARK_DRAFT = "MARK_DRAFT";
  public static final String RES_DISMISS = "DISMISS";
}
