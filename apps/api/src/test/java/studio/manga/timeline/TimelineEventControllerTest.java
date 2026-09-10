package studio.manga.timeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.manga.entities.StoryEntity;
import studio.manga.entities.StoryEntityRepository;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.Project;
import studio.manga.projects.ProjectRepository;
import studio.manga.timeline.TimelineEventController.EventInput;

class TimelineEventControllerTest {
  private TimelineEventRepository events;
  private StoryEntityRepository entities;
  private ProjectRepository projects;
  private CurrentUser user;
  private TimelineEventController controller;
  private UUID projectId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    events = mock(TimelineEventRepository.class);
    entities = mock(StoryEntityRepository.class);
    projects = mock(ProjectRepository.class);
    userId = UUID.randomUUID();
    user = new CurrentUser() {
      @Override
      public UUID id() {
        return userId;
      }
    };
    controller = new TimelineEventController(events, entities, projects, user);

    projectId = UUID.randomUUID();
    when(projects.findByIdAndOwnerId(projectId, userId))
        .thenReturn(Optional.of(new Project(userId, "Test Project", "Desc")));
  }

  @Test
  void createsEventWithLinkedEntitiesAndProvenance() {
    UUID entity1 = UUID.randomUUID();
    UUID docId = UUID.randomUUID();

    when(entities.findByIdAndProjectId(entity1, projectId))
        .thenReturn(Optional.of(new StoryEntity(projectId, "CHARACTER", "Aki", "", "CANON", null, "", "{}")));
    when(events.countByProjectId(projectId)).thenReturn(0);
    when(events.save(any(TimelineEvent.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    EventInput input =
        new EventInput(
            "The Fall of the Old Citadel",
            "Aki infiltrates the citadel as it crumbles.",
            "Pre-Calamity Year 99",
            1,
            docId,
            "CANON",
            Set.of(entity1));

    TimelineEvent created = controller.create(projectId, input);

    assertEquals("The Fall of the Old Citadel", created.title);
    assertEquals("Pre-Calamity Year 99", created.storyTime);
    assertEquals(1, created.orderIndex);
    assertEquals(docId, created.sourceDocumentId);
    assertEquals("CANON", created.status);
    assertTrue(created.entityIds.contains(entity1));
  }

  @Test
  void reordersEvents() {
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();

    TimelineEvent ev1 = new TimelineEvent(projectId, "Event 1", "", "", 1, null, "DRAFT", Set.of());
    ev1.id = id1;
    TimelineEvent ev2 = new TimelineEvent(projectId, "Event 2", "", "", 2, null, "DRAFT", Set.of());
    ev2.id = id2;

    when(events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId))
        .thenReturn(List.of(ev1, ev2));

    controller.reorder(projectId, List.of(id2, id1));

    assertEquals(2, ev1.orderIndex);
    assertEquals(1, ev2.orderIndex);
  }

  @Test
  void deletesEvent() {
    UUID eventId = UUID.randomUUID();
    TimelineEvent ev = new TimelineEvent(projectId, "Event", "", "", 1, null, "DRAFT", Set.of());
    when(events.findByIdAndProjectId(eventId, projectId)).thenReturn(Optional.of(ev));

    controller.delete(projectId, eventId);
    verify(events).delete(ev);
  }
}
