package rs.raf.examplatform.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.User;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByUsername(String username);
    java.util.List<User> findAllByRoleOrderByIdAsc(rs.raf.examplatform.model.Role role);
}
