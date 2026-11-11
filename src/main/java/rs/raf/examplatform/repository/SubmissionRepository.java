package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    List<Submission> findAllByAssignmentSessionId(Long sessionId);
}
