package studio.manga.entities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.manga.entities.StoryRelationshipController.RelationshipInput;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.Project;
import studio.manga.projects.ProjectRepository;

class StoryRelationshipControllerTest {
  private StoryRelationshipRepository relationships;
  private StoryEntityRepository entities;
  private ProjectRepository projects;
  private CurrentUser user;
  private StoryRelationshipController controller;
  private UUID projectId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    relationships = mock(StoryRelationshipRepository.class);
    entities = mock(StoryEntityRepository.class);
    projects = mock(ProjectRepository.class);
    userId = UUID.randomUUID();
    user = new CurrentUser() {
      @Override
      public UUID id() {
        return userId;
      }
    };
    controller = new StoryRelationshipController(relationships, entities, projects, user);

    projectId = UUID.randomUUID();
    when(projects.findByIdAndOwnerId(projectId, userId))
        .thenReturn(Optional.of(new Project(userId, "Test Project", "Desc")));
  }

  @Test
  void createsRelationshipWithProvenanceAndCanonStatus() {
    UUID sourceId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    UUID docId = UUID.randomUUID();

    StoryEntity source = new StoryEntity(projectId, "CHARACTER", "Aki", "", "CANON", null, "", "{}");
    StoryEntity target = new StoryEntity(projectId, "CHARACTER", "Ren", "", "CANON", null, "", "{}");

    when(entities.findByIdAndProjectId(sourceId, projectId)).thenReturn(Optional.of(source));
    when(entities.findByIdAndProjectId(targetId, projectId)).thenReturn(Optional.of(target));
    when(relationships.save(any(StoryRelationship.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RelationshipInput input =
        new RelationshipInput(
            sourceId,
            targetId,
            "sibling of",
            "Estranged brothers",
            "CANON",
            docId);

    StoryRelationship created = controller.create(projectId, input);

    assertEquals(sourceId, created.sourceEntityId);
    assertEquals(targetId, created.targetEntityId);
    assertEquals("sibling of", created.relationshipType);
    assertEquals("CANON", created.status);
    assertEquals(docId, created.sourceDocumentId);
  }

  @Test
  void rejectsSelfRelationship() {
    UUID entityId = UUID.randomUUID();
    RelationshipInput input =
        new RelationshipInput(entityId, entityId, "ally of", "", "CANON", null);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.create(projectId, input));
  }

  @Test
  void updatesRelationship() {
    UUID relId = UUID.randomUUID();
    UUID sourceId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();
    StoryRelationship existing =
        new StoryRelationship(projectId, sourceId, targetId, "friend of", "", "DRAFT", null);

    when(relationships.findByIdAndProjectId(relId, projectId)).thenReturn(Optional.of(existing));
    when(relationships.save(any(StoryRelationship.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    RelationshipInput updateInput =
        new RelationshipInput(null, null, "rival of", "Turned rivals after duel", "CANON", null);

    StoryRelationship updated = controller.update(projectId, relId, updateInput);

    assertEquals("rival of", updated.relationshipType);
    assertEquals("Turned rivals after duel", updated.description);
    assertEquals("CANON", updated.status);
  }

  @Test
  void deletesRelationship() {
    UUID relId = UUID.randomUUID();
    StoryRelationship existing =
        new StoryRelationship(projectId, UUID.randomUUID(), UUID.randomUUID(), "ally of", "", "CANON", null);

    when(relationships.findByIdAndProjectId(relId, projectId)).thenReturn(Optional.of(existing));

    controller.delete(projectId, relId);
    verify(relationships).delete(existing);
  }
}
