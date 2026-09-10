package studio.manga.memory;

import java.util.List;
import studio.manga.entities.StoryEntity;
import studio.manga.entities.StoryRelationship;
import studio.manga.timeline.TimelineEvent;

public record MemoryContext(
    List<StoryEntity> relevantEntities,
    List<StoryRelationship> relevantRelationships,
    List<TimelineEvent> relevantEvents,
    List<SemanticMemoryChunk> relevantChunks,
    String summary) {}
