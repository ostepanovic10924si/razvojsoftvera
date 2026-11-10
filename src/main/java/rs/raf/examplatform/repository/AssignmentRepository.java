package rs.raf.examplatform.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.Assignment;
public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    java.util.List<Assignment> findAllBySessionIdOrderByIdAsc(Long sessionId);
}
