package rs.raf.examplatform.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "exam_session_id"}))
public class ExamRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User student;
    @ManyToOne(optional = false)
    private ExamSession examSession;
    @Column(nullable = false)
    private Instant registeredAt = Instant.now();

    protected ExamRegistration() {
    }

    public ExamRegistration(User student, ExamSession examSession) {
        this.student = student;
        this.examSession = examSession;
    }

    public Long getId() {
        return id;
    }

    public User getStudent() {
        return student;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }
}
