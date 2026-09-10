package studio.manga.continuity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.manga.continuity.ContinuityController.ResolutionInput;
import studio.manga.entities.StoryEntity;
import studio.manga.entities.StoryEntityRepository;
import studio.manga.entities.StoryRelationship;
import studio.manga.entities.StoryRelationshipRepository;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.Project;
import studio.manga.projects.ProjectRepository;
import studio.manga.timeline.TimelineEvent;
import studio.manga.timeline.TimelineEventRepository;

class ContinuityControllerTest {
  private StoryEntityRepository entities;
  private StoryRelationshipRepository relationships;
  private TimelineEventRepository events;
  private ProjectRepository projects;
  private CurrentUser user;
  private ContinuityService continuityService;
  private ContinuityController controller;

  private UUID projectId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    entities = mock(StoryEntityRepository.class);
    relationships = mock(StoryRelationshipRepository.class);
    events = mock(TimelineEventRepository.class);
    projects = mock(ProjectRepository.class);

    userId = UUID.randomUUID();
    user = new CurrentUser() {
      @Override
      public UUID id() {
        return userId;
      }
    };

    continuityService = new ContinuityService(entities, relationships, events);
    controller = new ContinuityController(continuityService, projects, user);

    projectId = UUID.randomUUID();
    when(projects.findByIdAndOwnerId(projectId, userId))
        .thenReturn(Optional.of(new Project(userId, "Test Project", "Desc")));
  }

  @Test
  void detectsContradictedEntityNotice() {
    StoryEntity contradicted =
        new StoryEntity(projectId, "CHARACTER", "Aki", "", "CONTRADICTED", null, "", "{}");

    when(entities.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(List.of(contradicted));
    when(relationships.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(Collections.emptyList());
    when(events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId))
        .thenReturn(Collections.emptyList());

    List<ContinuityNotice> notices = controller.getNotices(projectId);

    assertEquals(1, notices.size());
    ContinuityNotice notice = notices.get(0);
    assertEquals(ContinuityNotice.TYPE_CONTRADICTION, notice.type());
    assertEquals(ContinuityNotice.SEVERITY_WARNING, notice.severity());
    assertTrue(notice.title().contains("Aki"));
    assertEquals(contradicted.id, notice.entityId());
  }

  @Test
  void detectsTimelineOrderAnomaly() {
    TimelineEvent ev1 = new TimelineEvent(projectId, "First Battle", "", "", 1, null, "CANON", Set.of());
    TimelineEvent ev2 = new TimelineEvent(projectId, "Second Battle", "", "", 1, null, "CANON", Set.of());

    when(entities.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(Collections.emptyList());
    when(relationships.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(Collections.emptyList());
    when(events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId))
        .thenReturn(List.of(ev1, ev2));

    List<ContinuityNotice> notices = controller.getNotices(projectId);

    assertEquals(1, notices.size());
    ContinuityNotice notice = notices.get(0);
    assertEquals(ContinuityNotice.TYPE_TIMELINE_ANOMALY, notice.type());
    assertTrue(notice.title().contains("#1"));
  }

  @Test
  void resolvesNoticeByMarkingCanon() {
    StoryEntity entity =
        new StoryEntity(projectId, "CHARACTER", "Ren", "", "CONTRADICTED", null, "", "{}");

    when(entities.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(List.of(entity));
    when(entities.findById(entity.id)).thenReturn(Optional.of(entity));
    when(relationships.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(Collections.emptyList());
    when(events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId))
        .thenReturn(Collections.emptyList());

    ResolutionInput input =
        new ResolutionInput(
            "ent-contra-" + entity.id,
            ContinuityNotice.RES_MARK_CANON,
            entity.id,
            null,
            null);

    controller.resolveNotice(projectId, input);

    assertEquals("CANON", entity.status);
    verify(entities).save(entity);
  }

  @Test
  void checksSceneTextForContradictions() {
    StoryEntity contradicted =
        new StoryEntity(projectId, "CHARACTER", "Aki", "", "CONTRADICTED", null, "", "{}");

    when(entities.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(List.of(contradicted));
    when(relationships.findByProjectIdOrderByUpdatedAtDesc(projectId))
        .thenReturn(Collections.emptyList());
    when(events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId))
        .thenReturn(Collections.emptyList());

    List<ContinuityNotice> notices =
        controller.checkScene(
            projectId,
            new ContinuityController.CheckInput("Aki entered the forbidden room with quiet footsteps."));

    assertFalse(notices.isEmpty());
    boolean foundTextNotice =
        notices.stream().anyMatch(n -> n.id().startsWith("text-contra-"));
    assertTrue(foundTextNotice);
  }
}
