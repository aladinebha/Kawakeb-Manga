package studio.manga.memory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.manga.documents.Document;
import studio.manga.documents.DocumentRepository;
import studio.manga.entities.StoryEntity;
import studio.manga.entities.StoryEntityRepository;
import studio.manga.entities.StoryRelationship;
import studio.manga.entities.StoryRelationshipRepository;
import studio.manga.timeline.TimelineEvent;
import studio.manga.timeline.TimelineEventRepository;

@Service
public class MemoryService {
  private final MemoryProposalRepository proposals;
  private final SemanticMemoryRepository semanticChunks;
  private final DocumentRepository documents;
  private final StoryEntityRepository entities;
  private final StoryRelationshipRepository relationships;
  private final TimelineEventRepository events;

  public MemoryService(
      MemoryProposalRepository proposals,
      SemanticMemoryRepository semanticChunks,
      DocumentRepository documents,
      StoryEntityRepository entities,
      StoryRelationshipRepository relationships,
      TimelineEventRepository events) {
    this.proposals = proposals;
    this.semanticChunks = semanticChunks;
    this.documents = documents;
    this.entities = entities;
    this.relationships = relationships;
    this.events = events;
  }

  @Transactional
  public List<MemoryProposal> extractFromDocument(UUID projectId, UUID documentId) {
    Document doc =
        documents
            .findByIdAndProjectId(documentId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Document not found"));

    String content = doc.content == null ? "" : doc.content;
    List<MemoryProposal> generated = new ArrayList<>();

    // Regex pattern matching capitalized candidate entity phrases (e.g. "Aki", "Captain Ren", "Citadel of Shadows")
    Pattern namePattern =
        Pattern.compile("\\b([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)\\b");
    Matcher matcher = namePattern.matcher(content);

    Set<String> existingEntityNames = new HashSet<>();
    for (StoryEntity e : entities.findByProjectIdOrderByUpdatedAtDesc(projectId)) {
      existingEntityNames.add(e.name.toLowerCase());
    }

    Set<String> stopWords =
        Set.of(
            "The", "A", "An", "In", "On", "At", "He", "She", "It", "They", "Then", "When", "There", "Here",
            "Chapter", "Scene", "Part", "After", "Before", "While", "But", "And", "So", "However", "Suddenly");

    Map<String, String> candidateSnippets = new LinkedHashMap<>();

    // Break into sentences for excerpt citations
    String[] sentences = content.split("(?<=[.!?])\\s+");

    for (String sentence : sentences) {
      Matcher m = namePattern.matcher(sentence);
      while (m.find()) {
        String candidate = m.group(1).trim();
        if (!stopWords.contains(candidate)
            && candidate.length() > 2
            && !existingEntityNames.contains(candidate.toLowerCase())) {
          candidateSnippets.putIfAbsent(candidate, sentence.trim());
        }
      }
    }

    for (Map.Entry<String, String> entry : candidateSnippets.entrySet()) {
      String candidateName = entry.getKey();
      String excerpt = entry.getValue();

      // Determine suggested type based on sentence clues
      String suggestedType = "CHARACTER";
      String lowerExcerpt = excerpt.toLowerCase();
      if (lowerExcerpt.contains("city")
          || lowerExcerpt.contains("temple")
          || lowerExcerpt.contains("citadel")
          || lowerExcerpt.contains("valley")
          || lowerExcerpt.contains("island")
          || lowerExcerpt.contains("room")
          || lowerExcerpt.contains("tower")) {
        suggestedType = "LOCATION";
      } else if (lowerExcerpt.contains("clan")
          || lowerExcerpt.contains("guild")
          || lowerExcerpt.contains("order")
          || lowerExcerpt.contains("division")
          || lowerExcerpt.contains("syndicate")
          || lowerExcerpt.contains("council")) {
        suggestedType = "ORGANIZATION";
      } else if (lowerExcerpt.contains("blade")
          || lowerExcerpt.contains("sword")
          || lowerExcerpt.contains("scroll")
          || lowerExcerpt.contains("relic")
          || lowerExcerpt.contains("crystal")
          || lowerExcerpt.contains("artifact")) {
        suggestedType = "OBJECT";
      }

      BigDecimal confidence =
          content.split("\\b" + Pattern.quote(candidateName) + "\\b").length > 2
              ? new BigDecimal("0.92")
              : new BigDecimal("0.82");

      MemoryProposal proposal =
          new MemoryProposal(
              projectId,
              documentId,
              "ENTITY",
              candidateName,
              suggestedType,
              "Extracted from " + doc.title + ": " + excerpt,
              excerpt,
              confidence);

      generated.add(proposals.save(proposal));
    }

    // Also extract semantic chunks from this document
    indexDocument(projectId, doc);

    return generated;
  }

  @Transactional
  public MemoryProposal approveProposal(UUID projectId, UUID proposalId) {
    MemoryProposal proposal =
        proposals
            .findByIdAndProjectId(proposalId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Memory proposal not found"));

    if ("ENTITY".equalsIgnoreCase(proposal.targetType)) {
      StoryEntity entity =
          new StoryEntity(
              projectId,
              proposal.suggestedType,
              proposal.name,
              proposal.suggestedDetails,
              "CANON",
              proposal.sourceDocumentId,
              proposal.sourceExcerpt,
              "{}");
      entities.save(entity);
    }

    proposal.status = "APPROVED";
    proposal.updatedAt = Instant.now();
    return proposals.save(proposal);
  }

  @Transactional
  public MemoryProposal rejectProposal(UUID projectId, UUID proposalId) {
    MemoryProposal proposal =
        proposals
            .findByIdAndProjectId(proposalId, projectId)
            .orElseThrow(() -> new NoSuchElementException("Memory proposal not found"));

    proposal.status = "REJECTED";
    proposal.updatedAt = Instant.now();
    return proposals.save(proposal);
  }

  @Transactional
  public void indexDocument(UUID projectId, Document doc) {
    if (doc.content == null || doc.content.isBlank()) {
      return;
    }

    semanticChunks.deleteByProjectIdAndSourceDocumentId(projectId, doc.id);

    // Chunk by paragraphs/scenes
    String[] sections = doc.content.split("\n\\s*\n");
    int sectionIndex = 1;

    for (String section : sections) {
      String clean = section.trim();
      if (clean.length() >= 20) {
        String title = doc.title + " (Scene " + (sectionIndex++) + ")";
        SemanticMemoryChunk chunk =
            new SemanticMemoryChunk(
                projectId,
                doc.id,
                null,
                "SCENE",
                title,
                clean,
                doc.type,
                "CANON");
        semanticChunks.save(chunk);
      }
    }
  }

  public MemoryContext retrieveContext(UUID projectId, String query, String selectedText) {
    String searchScope = ((query == null ? "" : query) + " " + (selectedText == null ? "" : selectedText)).trim();

    List<StoryEntity> allEntities = entities.findByProjectIdOrderByUpdatedAtDesc(projectId);
    List<StoryEntity> relevantEntities = new ArrayList<>();

    for (StoryEntity e : allEntities) {
      if (searchScope.isEmpty() || searchScope.toLowerCase().contains(e.name.toLowerCase())) {
        relevantEntities.add(e);
      }
    }

    List<StoryRelationship> allRels = relationships.findByProjectIdOrderByUpdatedAtDesc(projectId);
    List<StoryRelationship> relevantRels = new ArrayList<>();
    Set<UUID> relevantEntityIds = new HashSet<>();
    for (StoryEntity e : relevantEntities) {
      relevantEntityIds.add(e.id);
    }

    for (StoryRelationship r : allRels) {
      if (relevantEntityIds.contains(r.sourceEntityId) || relevantEntityIds.contains(r.targetEntityId)) {
        relevantRels.add(r);
      }
    }

    List<TimelineEvent> allEvents = events.findByProjectIdOrderByOrderIndexAscCreatedAtAsc(projectId);
    List<TimelineEvent> relevantEvents = new ArrayList<>();
    for (TimelineEvent ev : allEvents) {
      if (ev.entityIds != null) {
        for (UUID eid : ev.entityIds) {
          if (relevantEntityIds.contains(eid)) {
            relevantEvents.add(ev);
            break;
          }
        }
      }
    }

    List<SemanticMemoryChunk> chunks =
        searchScope.isEmpty()
            ? semanticChunks.findByProjectIdOrderByCreatedAtDesc(projectId)
            : semanticChunks.findByProjectIdAndContentContainingIgnoreCase(projectId, searchScope.split("\\s+")[0]);

    String summary =
        "Memory Context: "
            + relevantEntities.size()
            + " entities, "
            + relevantRels.size()
            + " relationships, "
            + relevantEvents.size()
            + " events, "
            + chunks.size()
            + " semantic chunks retrieved.";

    return new MemoryContext(relevantEntities, relevantRels, relevantEvents, chunks, summary);
  }
}
