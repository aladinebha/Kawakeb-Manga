package studio.manga.ai; import java.util.UUID; public record AiRequest(UUID projectId, String action, String selectedText, String instruction) {}
