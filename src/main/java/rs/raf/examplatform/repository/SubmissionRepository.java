package rs.raf.examplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rs.raf.examplatform.model.Submission;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    List<Submission> findAllByAssignmentSessionId(Long sessionId);

    List<Submission> findAllByStudentIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(Long studentId);

    /**
     * Submitted solutions matching all non-null criteria; a null criterion is ignored.
     */
    @Query("""
            select s from Submission s
            where s.submittedAt is not null
              and (:subjectId is null or s.assignment.session.subject.id = :subjectId)
              and (:examPeriodId is null or s.assignment.session.examPeriod.id = :examPeriodId)
              and (:sessionId is null or s.assignment.session.id = :sessionId)
              and (:groupId is null or s.assignment.session.examGroup.id = :groupId)
              and (:room is null or lower(s.assignment.session.examGroup.room) = lower(:room))
              and (:student is null
                   or lower(s.student.username) = lower(:student)
                   or lower(s.student.studentIndex) = lower(:student))
            order by s.assignment.session.startsAt, s.student.username, s.assignment.id
            """)
    List<Submission> search(@Param("subjectId") Long subjectId, @Param("examPeriodId") Long examPeriodId,
                            @Param("sessionId") Long sessionId, @Param("groupId") Long groupId,
                            @Param("room") String room, @Param("student") String student);
}
