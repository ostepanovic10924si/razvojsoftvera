package rs.raf.examplatform.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api") public class RoleController {
    public record Profile(String username, String role) {
    }

    @GetMapping("/student/me")
    public Profile student(Authentication auth) {
        return new Profile(auth.getName(), "STUDENT");
    }

    @GetMapping("/teacher/me")
    public Profile teacher(Authentication auth) {
        return new Profile(auth.getName(), "TEACHER");
    }
}
