package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
