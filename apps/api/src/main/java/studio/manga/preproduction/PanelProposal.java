package studio.manga.preproduction;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "panel_proposal")
public class PanelProposal {
  @Id
  public UUID id = UUID.randomUUID();

  @Column(name = "scene_id", nullable = false)
  public UUID sceneId;

  @Column(name = "order_index", nullable = false)
  public int orderIndex;

  @Column(name = "visual_prompt", nullable = false)
  public String visualPrompt;

  @Column
  public String dialogue;

  @Column(nullable = false)
  public String status = "PROPOSED";

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected PanelProposal() {}

  public PanelProposal(UUID sceneId, int orderIndex, String visualPrompt, String dialogue, String status) {
    this.sceneId = sceneId;
    this.orderIndex = orderIndex;
    this.visualPrompt = visualPrompt;
    this.dialogue = dialogue;
    this.status = status;
  }
}
