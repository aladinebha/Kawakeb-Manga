package studio.manga.identity;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="app_user") public class AppUser {
 @Id public UUID id=UUID.randomUUID(); @Column(nullable=false,unique=true) public String email; @Column(name="display_name",nullable=false) public String displayName; @Column(name="password_hash") public String passwordHash; @Column(name="created_at",nullable=false) public Instant createdAt=Instant.now();
 protected AppUser(){} public AppUser(String email,String displayName,String passwordHash){this.email=email.toLowerCase();this.displayName=displayName;this.passwordHash=passwordHash;}
}
