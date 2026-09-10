package studio.manga.identity;
import java.util.UUID; import org.springframework.stereotype.Component;
/** Temporary local identity adapter. Replace with managed OIDC/JWT validation without changing domain services. */
@Component public class CurrentUser { public static final UUID DEMO_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001"); public UUID id() { return DEMO_USER_ID; } }
