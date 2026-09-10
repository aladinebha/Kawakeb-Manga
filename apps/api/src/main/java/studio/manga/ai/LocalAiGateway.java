package studio.manga.ai;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import studio.manga.memory.MemoryContext;
import studio.manga.memory.MemoryService;

/** Safe development provider; swap this adapter, not business code, for a hosted provider. */
@Component
@Primary
public class LocalAiGateway implements AiGateway {
  private final MemoryService memoryService;

  public LocalAiGateway() {
    this.memoryService = null;
  }

  @Autowired
  public LocalAiGateway(MemoryService memoryService) {
    this.memoryService = memoryService;
  }

  @Override
  public AiProposal propose(AiRequest r) {
    String basis =
        r.selectedText() == null || r.selectedText().isBlank()
            ? "your current scene"
            : r.selectedText();

    String memoryNote = "This is a proposal only. It has not changed your document or project canon.";
    String extraContext = "";

    if (memoryService != null && r.projectId() != null) {
      MemoryContext ctx = memoryService.retrieveContext(r.projectId(), r.action(), r.selectedText());
      if (!ctx.relevantEntities().isEmpty() || !ctx.relevantChunks().isEmpty()) {
        List<String> names = ctx.relevantEntities().stream().map(e -> e.name).limit(3).toList();
        if (!names.isEmpty()) {
          extraContext = " Grounded in Atlas: " + String.join(", ", names) + ".";
        }
        memoryNote += " " + ctx.summary();
      }
    }

    String content =
        switch (r.action().toUpperCase()) {
          case "CONTINUE" ->
              "A possible next beat:\n\nThe silence after "
                  + basis
                  + " became its own answer."
                  + (extraContext.isEmpty()
                      ? " Aki paused at the threshold, listening for the sound that had brought him here."
                      : extraContext + " The path ahead opened with unspoken tension.");
          case "DIALOGUE" ->
              "Alternative dialogue:\n\n\"You came anyway,\" Hana said.\n\"I said I would,\" Aki replied, though neither of them believed it was that simple."
                  + (extraContext.isEmpty() ? "" : "\n(" + extraContext + ")");
          default ->
              "Idea to explore:\n\nWhat does this moment cost the character, and what new question does it leave unresolved? Build the next beat around that tension."
                  + (extraContext.isEmpty() ? "" : "\n\n" + extraContext);
        };

    return new AiProposal(content, "AI_SUGGESTION", memoryNote);
  }
}
