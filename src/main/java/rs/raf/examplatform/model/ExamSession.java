package rs.raf.examplatform.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity public class ExamSession {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private Instant startsAt;
    @Column(nullable=false) private Instant endsAt;
    @ManyToOne(optional=false) private Subject subject;
    @ManyToOne(optional=false) private ExamPeriod examPeriod;
    @ManyToOne(optional=false) private ExamGroup examGroup;
    protected ExamSession() {}
    public ExamSession(String name,Instant startsAt,Instant endsAt,Subject subject,ExamPeriod examPeriod,ExamGroup examGroup){this.name=name;this.startsAt=startsAt;this.endsAt=endsAt;this.subject=subject;this.examPeriod=examPeriod;this.examGroup=examGroup;}
    public Long getId(){return id;} public String getName(){return name;} public Instant getStartsAt(){return startsAt;} public Instant getEndsAt(){return endsAt;}
    public Subject getSubject(){return subject;} public ExamPeriod getExamPeriod(){return examPeriod;} public ExamGroup getExamGroup(){return examGroup;}
}
