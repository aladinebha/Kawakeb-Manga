package studio.manga.preproduction;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface PanelProposalRepository extends CrudRepository<PanelProposal, UUID> {
  List<PanelProposal> findBySceneIdOrderByOrderIndexAsc(UUID sceneId);
}
