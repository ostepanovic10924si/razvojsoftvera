package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.ExamSession;

import java.time.Instant;
import java.util.List;

public interface ExamSessionRepository extends JpaRepository<ExamSession, Long> {
    List<ExamSession> findAllByEndsAtAfterOrderByStartsAtAsc(Instant now);
}
