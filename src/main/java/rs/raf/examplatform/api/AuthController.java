package rs.raf.examplatform.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.examplatform.model.User;
import rs.raf.examplatform.repository.UserRepository;
import rs.raf.examplatform.security.JwtService;

@RestController @RequestMapping("/api/auth") public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, String tokenType, String username, String role) {
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        User user = users.findByUsername(request.username())
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Neispravno korisničko ime ili lozinka"));
        return new LoginResponse(jwt.create(user), "Bearer", user.getUsername(), user.getRole().name());
    }
}
