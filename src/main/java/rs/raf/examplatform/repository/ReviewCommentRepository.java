package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.ReviewComment;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findAllBySubmissionIdOrderByFilePathAscStartLineAscIdAsc(Long submissionId);
}
