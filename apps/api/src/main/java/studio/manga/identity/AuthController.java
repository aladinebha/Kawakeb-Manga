package studio.manga.identity;
import jakarta.servlet.http.HttpSession; import org.springframework.http.*; import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/auth") public class AuthController {
 private final AppUserRepository users; private final BCryptPasswordEncoder passwords=new BCryptPasswordEncoder();
 AuthController(AppUserRepository users){this.users=users;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public UserView register(@RequestBody Credentials input,HttpSession session){validate(input); if(users.existsByEmailIgnoreCase(input.email())) throw new IllegalArgumentException("An account already exists for this email."); AppUser user=users.save(new AppUser(input.email().trim(),input.displayName().trim(),passwords.encode(input.password()))); session.setAttribute("userId",user.id); return view(user);}
 @PostMapping("/login") public UserView login(@RequestBody Login input,HttpSession session){AppUser user=users.findByEmailIgnoreCase(input.email().trim()).filter(u->u.passwordHash!=null&&passwords.matches(input.password(),u.passwordHash)).orElseThrow(UnauthorizedException::new); session.setAttribute("userId",user.id); return view(user);}
 @GetMapping("/me") public UserView me(HttpSession session){Object id=session.getAttribute("userId"); if(!(id instanceof UUID userId))throw new UnauthorizedException(); return users.findById(userId).map(this::view).orElseThrow(UnauthorizedException::new);}
 @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(HttpSession session){session.invalidate();}
 private void validate(Credentials input){if(input.email()==null||!input.email().contains("@")||input.password()==null||input.password().length()<8||input.displayName()==null||input.displayName().isBlank())throw new IllegalArgumentException("Enter a name, valid email, and password of at least 8 characters.");}
 private UserView view(AppUser user){return new UserView(user.id,user.email,user.displayName);} public record Credentials(String email,String password,String displayName){} public record Login(String email,String password){} public record UserView(UUID id,String email,String displayName){}
}
