package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rs.raf.examplatform.model.ExamPeriod;

public interface ExamPeriodRepository extends JpaRepository<ExamPeriod, Long> {
}
