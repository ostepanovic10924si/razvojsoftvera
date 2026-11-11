package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.ReviewComment;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
}
