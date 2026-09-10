package studio.manga.memory;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryProposalRepository extends JpaRepository<MemoryProposal, UUID> {
  List<MemoryProposal> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
  List<MemoryProposal> findByProjectIdAndStatusOrderByCreatedAtDesc(UUID projectId, String status);
  Optional<MemoryProposal> findByIdAndProjectId(UUID id, UUID projectId);
}
