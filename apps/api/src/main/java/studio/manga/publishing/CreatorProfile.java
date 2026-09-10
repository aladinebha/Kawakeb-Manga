package studio.manga.publishing;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "creator_profile")
public class CreatorProfile {
  @Id
  @Column(name = "user_id")
  public UUID userId;

  @Column(name = "pen_name", nullable = false)
  public String penName;

  @Column(nullable = false)
  public String bio = "";

  @Column(name = "avatar_url")
  public String avatarUrl;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  public Instant updatedAt = Instant.now();

  protected CreatorProfile() {}

  public CreatorProfile(UUID userId, String penName, String bio, String avatarUrl) {
    this.userId = userId;
    this.penName = penName;
    this.bio = bio;
    this.avatarUrl = avatarUrl;
  }
}
