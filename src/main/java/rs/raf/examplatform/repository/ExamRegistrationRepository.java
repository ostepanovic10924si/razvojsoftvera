package rs.raf.examplatform.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.ExamRegistration;
public interface ExamRegistrationRepository extends JpaRepository<ExamRegistration,Long> {
    boolean existsByStudentIdAndExamSessionId(Long studentId, Long examSessionId);
    java.util.List<ExamRegistration> findAllByExamSessionIdOrderByIdAsc(Long examSessionId);
}
