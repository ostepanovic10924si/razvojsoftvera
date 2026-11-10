package rs.raf.examplatform.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.ExamSession;
public interface ExamSessionRepository extends JpaRepository<ExamSession,Long> {}
