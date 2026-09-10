package studio.manga.ai;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID; import org.junit.jupiter.api.Test;
class LocalAiGatewayTest {
 @Test void proposalsAreAlwaysClearlyNonCanonical() {
  AiProposal proposal=new LocalAiGateway().propose(new AiRequest(UUID.randomUUID(),"CONTINUE","Aki opened the door.",""));
  assertEquals("AI_SUGGESTION",proposal.authority());
  assertTrue(proposal.note().contains("not changed"));
 }
}
