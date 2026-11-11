package rs.raf.examplatform.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "assignment_id"}))
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private User student;
    @ManyToOne(optional = false)
    private Assignment assignment;
    @Column(nullable = false)
    private Instant downloadedAt;
    private Instant submittedAt;
    @Column(length = 2000)
    private String repositoryUrl;
    private Double points;

    protected Submission() {
    }

    public Submission(User student, Assignment assignment, Instant downloadedAt, String repositoryUrl) {
        this.student = student;
        this.assignment = assignment;
        this.downloadedAt = downloadedAt;
        this.repositoryUrl = repositoryUrl;
    }

    public Long getId() {
        return id;
    }

    public User getStudent() {
        return student;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public Instant getDownloadedAt() {
        return downloadedAt;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public Double getPoints() {
        return points;
    }

    public void markSubmitted(String repositoryUrl, Instant submittedAt) {
        this.repositoryUrl = repositoryUrl;
        this.submittedAt = submittedAt;
    }
}
