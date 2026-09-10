package studio.manga.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.manga.entities.StoryEntityController.EntityInput;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.Project;
import studio.manga.projects.ProjectRepository;

class StoryEntityControllerTest {
  private StoryEntityRepository entities;
  private StoryRelationshipRepository relationships;
  private ProjectRepository projects;
  private CurrentUser user;
  private StoryEntityController controller;
  private UUID projectId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    entities = mock(StoryEntityRepository.class);
    relationships = mock(StoryRelationshipRepository.class);
    projects = mock(ProjectRepository.class);
    userId = UUID.randomUUID();
    user = new CurrentUser() {
      @Override
      public UUID id() {
        return userId;
      }
    };
    controller = new StoryEntityController(entities, relationships, projects, user);

    projectId = UUID.randomUUID();
    when(projects.findByIdAndOwnerId(projectId, userId))
        .thenReturn(Optional.of(new Project(userId, "Test Project", "Desc")));
  }

  @Test
  void createsEntityWithProvenanceAndAttributes() {
    UUID docId = UUID.randomUUID();
    EntityInput input =
        new EntityInput(
            "CHARACTER",
            "Aki",
            "Protagonist with shadow manipulation",
            "CANON",
            docId,
            "Chapter 1: Aki appeared in the alley.",
            "{\"role\":\"Protagonist\",\"abilities\":\"Shadow manipulation\"}");

    when(entities.save(any(StoryEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    StoryEntity created = controller.create(projectId, input);

    assertEquals("CHARACTER", created.type);
    assertEquals("Aki", created.name);
    assertEquals("CANON", created.status);
    assertEquals(docId, created.sourceDocumentId);
    assertEquals("Chapter 1: Aki appeared in the alley.", created.sourceExcerpt);
    assertTrue(created.attributes.contains("Shadow manipulation"));
  }

  @Test
  void updatesEntityPreservingAttributesAndExcerpt() {
    UUID entityId = UUID.randomUUID();
    StoryEntity existing =
        new StoryEntity(
            projectId,
            "CHARACTER",
            "Old Name",
            "Old Desc",
            "DRAFT",
            null,
            "",
            "{}");
    when(entities.findByIdAndProjectId(entityId, projectId))
        .thenReturn(Optional.of(existing));
    when(entities.save(any(StoryEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    EntityInput input =
        new EntityInput(
            "CHARACTER",
            "Ren",
            "Updated Desc",
            "CANON",
            UUID.randomUUID(),
            "Chapter 2: Ren stepped forward.",
            "{\"role\":\"Rival\"}");

    StoryEntity updated = controller.update(projectId, entityId, input);

    assertEquals("Ren", updated.name);
    assertEquals("CANON", updated.status);
    assertEquals("Chapter 2: Ren stepped forward.", updated.sourceExcerpt);
    assertEquals("{\"role\":\"Rival\"}", updated.attributes);
  }

  @Test
  void rejectsInvalidCanonStatus() {
    EntityInput input =
        new EntityInput(
            "CHARACTER",
            "Aki",
            "Desc",
            "INVALID_STATUS",
            null,
            "",
            "{}");

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.create(projectId, input));
  }

  @Test
  void deletesEntity() {
    UUID entityId = UUID.randomUUID();
    StoryEntity existing =
        new StoryEntity(projectId, "LOCATION", "Tokyo", "Desc", "CANON", null, "", "{}");
    when(entities.findByIdAndProjectId(entityId, projectId))
        .thenReturn(Optional.of(existing));

    controller.delete(projectId, entityId);
    verify(entities).delete(existing);
  }
}
