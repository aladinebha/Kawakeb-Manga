package studio.manga.ai;
import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/projects/{projectId}/ai") public class AiController { private final AiGateway gateway; AiController(AiGateway gateway){this.gateway=gateway;} @PostMapping("/proposals") public AiProposal propose(@PathVariable UUID projectId,@RequestBody ProposalInput in){return gateway.propose(new AiRequest(projectId,in.action(),in.selectedText(),in.instruction()));} public record ProposalInput(String action,String selectedText,String instruction){} }
