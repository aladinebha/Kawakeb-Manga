package studio.manga.continuity;

import java.util.*;
import org.springframework.stereotype.Service;
import studio.manga.entities.StoryEntity;
import studio.manga.entities.StoryEntityRepository;
import studio.manga.entities.StoryRelationship;
import studio.manga.entities.StoryRelationshipRepository;
import studio.manga.timeline.TimelineEvent;
import studio.manga.timeline.TimelineEventRepository;

@Service
public class ContinuityService {
  private final StoryEntityRepository entities;
  private final StoryRelationshipRepository relationships;
  private final TimelineEventRepository events;

  // In-memory dismissed notice IDs for non-blocking creator dismissals
  private final Set<String> dismissedNotices = Collections.synchronizedSet(new HashSet<>());

  public ContinuityService(
      StoryEntityRepository entities,
      StoryRelationshipRepository relationships,
      TimelineEventRepository events) {
    this.entities = entities;
    this.relationships = relationships;
    this.events = events;
  }

  public List<ContinuityNotice> checkProject(UUID projectId) {
    List<ContinuityNotice> notices = new ArrayList<>();

    List<StoryEntity> entityList = entities.findByProjectIdOrderByUpdatedAtDesc(projectId);
    Map<UUID, StoryEntity> entityMap = new HashMap<>();
    for (StoryEntity e : entityList) {
      entityMap.put(e.id, e);
    }

    // 1. Entity Checks
    for (StoryEntity entity : entityList) {
      if ("CONTRADICTED".equalsIgnoreCase(entity.status)) {
        String noticeId = "ent-contra-" + entity.id;
        if (!dismissedNotices.contains(noticeId)) {
          notices.add(
              new ContinuityNotice(
                  noticeId,
                  ContinuityNotice.TYPE_CONTRADICTION,
                  ContinuityNotice.SEVERITY_WARNING,
                  "Contradicted Story Entry: " + entity.name,
                  "\"" + entity.name + "\" (" + entity.type + ") is marked as CONTRADICTED. Creator can accept as retcon or keep as intentional contradiction.",
                  entity.id,
                  null,
                  null,
                  entity.sourceDocumentId,
                  List.of(ContinuityNotice.RES_MARK_CANON, ContinuityNotice.RES_MARK_DRAFT, ContinuityNotice.RES_DISMISS)));
        }
      } else if ("CANON".equalsIgnoreCase(entity.status)
          && entity.sourceDocumentId == null
          && (entity.sourceExcerpt == null || entity.sourceExcerpt.isBlank())) {
        String noticeId = "ent-unverified-" + entity.id;
        if (!dismissedNotices.contains(noticeId)) {
          notices.add(
              new ContinuityNotice(
                  noticeId,
                  ContinuityNotice.TYPE_LORE_DISCREPANCY,
                  ContinuityNotice.SEVERITY_INFO,
                  "Unlinked Canon Entry: " + entity.name,
                  "\"" + entity.name + "\" is marked CANON but lacks a linked manuscript source document or direct citation.",
                  entity.id,
                  null,
                  null,
                  null,
                  List.of(ContinuityNotice.RES_DISMISS)));
        }
      }
    }

    // 2. Relationship Checks
    List<StoryRelationship> relList = relationships.findByProjectIdOrderByUpdatedAtDesc(projectId);
    Map<String, List<StoryRelationship>> pairMap = new HashMap<>();

    for (StoryRelationship rel : relList) {
      if ("CONTRADICTED".equalsIgnoreCase(rel.status)) {
        String noticeId = "rel-contra-" + rel.id;
        if (!dismissedNotices.contains(noticeId)) {
          notices.add(
              new ContinuityNotice(
                  noticeId,
                  ContinuityNotice.TYPE_RELATIONSHIP_CONFLICT,
                  ContinuityNotice.SEVERITY_WARNING,
                  "Contradicted Relationship: " + rel.relationshipType,
                  "The connection between " + name(entityMap, rel.sourceEntityId) + " and " + name(entityMap, rel.targetEntityId) + " is marked as CONTRADICTED.",
                  null,
                  rel.id,
                  null,
                  rel.sourceDocumentId,
                  List.of(ContinuityNotice.RES_MARK_CANON, ContinuityNotice.RES_MARK_DRAFT, ContinuityNotice.RES_DISMISS)));
        }
      }

      // Group by unordered entity pair to detect conflicting relationships
      String pairKey = rel.sourceEntityId.compareTo(rel.targetEntityId) < 0
          ? rel.sourceEntityId + ":" + rel.targetEntityId
          : rel.targetEntityId + ":" + rel.sourceEntityId;
      pairMap.computeIfAbsent(pairKey, k -> new ArrayList<>()).add(rel);
    }

    for (Map.Entry<String, List<StoryRelationship>> entry : pairMap.entrySet()) {
      List<StoryRelationship> pairRels = entry.getValue();
      if (pairRels.size() > 1) {
        // Check if types or statuses conflict (e.g. ally vs enemy)
        Set<String> types = new HashSet<>();
        for (StoryRelationship r : pairRels) {
          types.add(r.relationshipType.toLowerCase());
        }
        if ((types.contains("ally") || types.contains("allied with") || types.contains("friend of"))
            && (types.contains("enemy") || types.contains("enemy of") || types.contains("rival of"))) {
          StoryRelationship sample = pairRels.get(0);
          String noticeId = "rel-conflict-" + entry.getKey();
          if (!dismissedNotices.contains(noticeId)) {
            notices.add(
                new ContinuityNotice(
                    noticeId,
                    ContinuityNotice.TYPE_RELATIONSHIP_CONFLICT,
                    ContinuityNotice.SEVERITY_WARNING,
                    "Conflicting Relationship Dynamics",
                    name(entityMap, sample.sourceEntityId) + " and " + name(entityMap, sample.targetEntityId) + " have both allied and adversarial relationships recorded concurrently.",
                    null,
                    sample.id,
                    null,
                    null,
                    List.of(ContinuityNotice.RES_DISMISS)));
          }
        }
      }
    }

    // 3. Timeline Event Checks
    List<TimelineEvent> eventList = events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId);
    Set<Integer> seenIndices = new HashSet<>();

    for (TimelineEvent ev : eventList) {
      if (!seenIndices.add(ev.orderIndex)) {
        String noticeId = "time-order-dup-" + ev.orderIndex;
        if (!dismissedNotices.contains(noticeId)) {
          notices.add(
              new ContinuityNotice(
                  noticeId,
                  ContinuityNotice.TYPE_TIMELINE_ANOMALY,
                  ContinuityNotice.SEVERITY_INFO,
                  "Ambiguous Chronological Order: #" + ev.orderIndex,
                  "Multiple timeline events share order index #" + ev.orderIndex + " (including \"" + ev.title + "\"). Consider establishing sequential ordering.",
                  null,
                  null,
                  ev.id,
                  ev.sourceDocumentId,
                  List.of(ContinuityNotice.RES_DISMISS)));
        }
      }

      if (ev.entityIds != null) {
        for (UUID eid : ev.entityIds) {
          StoryEntity linked = entityMap.get(eid);
          if (linked != null && "CONTRADICTED".equalsIgnoreCase(linked.status)) {
            String noticeId = "time-contra-ent-" + ev.id + "-" + eid;
            if (!dismissedNotices.contains(noticeId)) {
              notices.add(
                  new ContinuityNotice(
                      noticeId,
                      ContinuityNotice.TYPE_TIMELINE_ANOMALY,
                      ContinuityNotice.SEVERITY_WARNING,
                      "Timeline Contradiction: " + ev.title,
                      "Event \"" + ev.title + "\" includes \"" + linked.name + "\", which is currently marked as CONTRADICTED.",
                      linked.id,
                      null,
                      ev.id,
                      ev.sourceDocumentId,
                      List.of(ContinuityNotice.RES_MARK_CANON, ContinuityNotice.RES_DISMISS)));
            }
          }
        }
      }
    }

    return notices;
  }

  public List<ContinuityNotice> checkText(UUID projectId, String text) {
    List<ContinuityNotice> baseNotices = checkProject(projectId);
    if (text == null || text.isBlank()) {
      return baseNotices;
    }

    List<ContinuityNotice> result = new ArrayList<>(baseNotices);
    List<StoryEntity> entityList = entities.findByProjectIdOrderByUpdatedAtDesc(projectId);
    String lowerText = text.toLowerCase();

    for (StoryEntity ent : entityList) {
      if (lowerText.contains(ent.name.toLowerCase())) {
        if ("CONTRADICTED".equalsIgnoreCase(ent.status)) {
          result.add(
              new ContinuityNotice(
                  "text-contra-" + ent.id,
                  ContinuityNotice.TYPE_CONTRADICTION,
                  ContinuityNotice.SEVERITY_WARNING,
                  "Scene References Contradicted Entry: " + ent.name,
                  "The current scene text mentions \"" + ent.name + "\", which is marked as CONTRADICTED in your Story Atlas.",
                  ent.id,
                  null,
                  null,
                  null,
                  List.of(ContinuityNotice.RES_MARK_CANON, ContinuityNotice.RES_DISMISS)));
        } else if ("IDEA".equalsIgnoreCase(ent.status) || "DRAFT".equalsIgnoreCase(ent.status)) {
          result.add(
              new ContinuityNotice(
                  "text-draft-" + ent.id,
                  ContinuityNotice.TYPE_LORE_DISCREPANCY,
                  ContinuityNotice.SEVERITY_INFO,
                  "Unconfirmed Lore in Scene: " + ent.name,
                  "\"" + ent.name + "\" appears in this manuscript scene but is currently marked as " + ent.status + " in your Atlas.",
                  ent.id,
                  null,
                  null,
                  null,
                  List.of(ContinuityNotice.RES_MARK_CANON, ContinuityNotice.RES_DISMISS)));
        }
      }
    }

    return result;
  }

  public void dismiss(String noticeId) {
    if (noticeId != null && !noticeId.isBlank()) {
      dismissedNotices.add(noticeId);
    }
  }

  public void resolveEntity(UUID entityId, String targetStatus) {
    entities.findById(entityId).ifPresent(e -> {
      e.status = targetStatus;
      entities.save(e);
    });
  }

  public void resolveRelationship(UUID relationshipId, String targetStatus) {
    relationships.findById(relationshipId).ifPresent(r -> {
      r.status = targetStatus;
      relationships.save(r);
    });
  }

  private String name(Map<UUID, StoryEntity> map, UUID id) {
    StoryEntity e = map.get(id);
    return e != null ? e.name : "Unknown Entry";
  }
}
