package studio.manga.memory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.manga.documents.Document;
import studio.manga.documents.DocumentRepository;
import studio.manga.entities.StoryEntity;
import studio.manga.entities.StoryEntityRepository;
import studio.manga.entities.StoryRelationship;
import studio.manga.entities.StoryRelationshipRepository;
import studio.manga.identity.CurrentUser;
import studio.manga.projects.Project;
import studio.manga.projects.ProjectRepository;
import studio.manga.timeline.TimelineEvent;
import studio.manga.timeline.TimelineEventRepository;

class MemoryControllerTest {
  private MemoryProposalRepository proposals;
  private SemanticMemoryRepository semanticChunks;
  private DocumentRepository documents;
  private StoryEntityRepository entities;
  private StoryRelationshipRepository relationships;
  private TimelineEventRepository events;
  private ProjectRepository projects;
  private CurrentUser user;

  private MemoryService memoryService;
  private MemoryController controller;

  private UUID projectId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    proposals = mock(MemoryProposalRepository.class);
    semanticChunks = mock(SemanticMemoryRepository.class);
    documents = mock(DocumentRepository.class);
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

    memoryService =
        new MemoryService(
            proposals, semanticChunks, documents, entities, relationships, events);
    controller =
        new MemoryController(memoryService, proposals, semanticChunks, projects, user);

    projectId = UUID.randomUUID();
    when(projects.findByIdAndOwnerId(projectId, userId))
        .thenReturn(Optional.of(new Project(userId, "Test Project", "Desc")));
  }

  @Test
  void extractsCandidateFactsFromDocument() {
    UUID docId = UUID.randomUUID();
    Document doc = new Document(projectId, null, "Chapter 1: The Outpost", "CHAPTER");
    doc.content =
        "Captain Ren stood before the gates of Citadel. The wind carried whispers of dark relics.";

    when(documents.findByIdAndProjectId(docId, projectId)).thenReturn(Optional.of(doc));
    when(entities.findByProjectIdOrderByUpdatedAtDesc(projectId)).thenReturn(Collections.emptyList());
    when(proposals.save(any(MemoryProposal.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    List<MemoryProposal> result =
        controller.extract(projectId, new MemoryController.ExtractInput(docId));

    assertFalse(result.isEmpty());
    boolean hasRen = result.stream().anyMatch(p -> p.name.contains("Captain Ren") || p.name.contains("Ren"));
    assertTrue(hasRen);
    // Verifies sentence citation
    for (MemoryProposal p : result) {
      assertFalse(p.sourceExcerpt.isBlank());
      assertNotNull(p.confidence);
      assertEquals("PENDING", p.status);
    }
  }

  @Test
  void approvesProposalPromotingToAtlas() {
    UUID proposalId = UUID.randomUUID();
    UUID docId = UUID.randomUUID();
    MemoryProposal proposal =
        new MemoryProposal(
            projectId,
            docId,
            "ENTITY",
            "Lady Sora",
            "CHARACTER",
            "High Priestess of the Lotus",
            "Lady Sora smiled warmly.",
            null);
    proposal.id = proposalId;

    when(proposals.findByIdAndProjectId(proposalId, projectId)).thenReturn(Optional.of(proposal));
    when(proposals.save(any(MemoryProposal.class))).thenAnswer(inv -> inv.getArgument(0));
    when(entities.save(any(StoryEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    MemoryProposal approved = controller.approve(projectId, proposalId);

    assertEquals("APPROVED", approved.status);
    verify(entities).save(argThat(e ->
        e.name.equals("Lady Sora")
            && e.type.equals("CHARACTER")
            && "CANON".equals(e.status)
            && docId.equals(e.sourceDocumentId)));
  }

  @Test
  void rejectsProposal() {
    UUID proposalId = UUID.randomUUID();
    MemoryProposal proposal =
        new MemoryProposal(
            projectId,
            UUID.randomUUID(),
            "ENTITY",
            "Unwanted Fact",
            "CONCEPT",
            "",
            "",
            null);

    when(proposals.findByIdAndProjectId(proposalId, projectId)).thenReturn(Optional.of(proposal));
    when(proposals.save(any(MemoryProposal.class))).thenAnswer(inv -> inv.getArgument(0));

    MemoryProposal rejected = controller.reject(projectId, proposalId);

    assertEquals("REJECTED", rejected.status);
  }

  @Test
  void retrievesHybridMemoryContext() {
    StoryEntity aki = new StoryEntity(projectId, "CHARACTER", "Aki", "Shadow user", "CANON", null, "", "{}");
    when(entities.findByProjectIdOrderByUpdatedAtDesc(projectId)).thenReturn(List.of(aki));
    when(relationships.findByProjectIdOrderByUpdatedAtDesc(projectId)).thenReturn(Collections.emptyList());
    when(events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId)).thenReturn(Collections.emptyList());
    when(semanticChunks.findByProjectIdAndContentContainingIgnoreCase(eq(projectId), anyString()))
        .thenReturn(Collections.emptyList());

    MemoryContext ctx =
        controller.retrieve(projectId, new MemoryController.RetrieveInput("Aki at the bridge", ""));

    assertEquals(1, ctx.relevantEntities().size());
    assertEquals("Aki", ctx.relevantEntities().get(0).name);
    assertTrue(ctx.summary().contains("1 entities"));
  }
}
