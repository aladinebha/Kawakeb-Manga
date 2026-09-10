package studio.manga.identity;
import java.util.UUID; import jakarta.servlet.http.HttpServletRequest; import org.springframework.stereotype.Component; import org.springframework.web.context.request.*;
/** Session adapter. A managed OIDC/JWT adapter can replace this without changing domain services. */
@Component public class CurrentUser { public UUID id() { HttpServletRequest request=((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest(); Object id=request.getSession(false)==null?null:request.getSession(false).getAttribute("userId"); if(id instanceof UUID value)return value; throw new UnauthorizedException(); } }
