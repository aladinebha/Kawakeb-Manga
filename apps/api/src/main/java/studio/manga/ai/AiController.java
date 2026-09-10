package studio.manga.ai;

import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/ai")
public class AiController {
    private final AiGateway gateway;

    public AiController(AiGateway gateway) {
        this.gateway = gateway;
    }

    @PostMapping("/proposals")
    public AiProposal propose(@PathVariable UUID projectId, @RequestBody ProposalInput in) {
        return gateway.propose(new AiRequest(projectId, in.action(), in.selectedText(), in.instruction()));
    }

    @PostMapping("/generate-image")
    public Map<String, String> generateImage(@PathVariable UUID projectId, @RequestBody ImageGenerationInput in) {
        // Vertex AI / Imagen integration would go here.
        // For now, since Gemini 2.5 Flash doesn't return raw images and Imagen requires Vertex SDK,
        // we return a placeholder URL simulating the AI generating an image.
        String dummyUrl = "https://placehold.co/600x400/222222/cccccc.png?text=AI+Generated+Image\\n" 
                + in.prompt().replaceAll(" ", "+");
        return Map.of("url", dummyUrl);
    }

    public record ProposalInput(String action, String selectedText, String instruction) {}
    public record ImageGenerationInput(String prompt) {}
}
