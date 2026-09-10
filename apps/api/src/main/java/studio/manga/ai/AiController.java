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
        // Enforce manga style in the prompt
        String mangaStylePrompt = "black and white manga style, ink pen, screentone, monochrome line art, anime style, " 
                                + in.prompt();
        
        // Pollinations.ai provides a free, open image generation endpoint
        String encodedPrompt = java.net.URLEncoder.encode(mangaStylePrompt, java.nio.charset.StandardCharsets.UTF_8);
        String imageUrl = "https://image.pollinations.ai/prompt/" + encodedPrompt + "?width=800&height=1200&nologo=true";
        
        return Map.of("url", imageUrl);
    }

    public record ProposalInput(String action, String selectedText, String instruction) {}
    public record ImageGenerationInput(String prompt) {}
}
