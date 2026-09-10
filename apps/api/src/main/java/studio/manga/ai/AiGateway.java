package studio.manga.ai;
/** Provider-neutral boundary. Domain code depends on this interface, never an AI vendor SDK. */
public interface AiGateway { AiProposal propose(AiRequest request); }
