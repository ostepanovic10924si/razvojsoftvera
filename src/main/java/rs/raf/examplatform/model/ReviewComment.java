package rs.raf.examplatform.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity public class ReviewComment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private Submission submission;
    @ManyToOne(optional=false) private User teacher;
    @Column(nullable=false,length=4000) private String body;
    private String filePath; private Integer startLine; private Integer endLine;
    @Column(nullable=false) private Instant createdAt=Instant.now();
    protected ReviewComment() {}
    public ReviewComment(Submission submission,User teacher,String body,String filePath,Integer startLine,Integer endLine){this.submission=submission;this.teacher=teacher;this.body=body;this.filePath=filePath;this.startLine=startLine;this.endLine=endLine;}
    public Long getId(){return id;} public Submission getSubmission(){return submission;} public User getTeacher(){return teacher;} public String getBody(){return body;} public String getFilePath(){return filePath;} public Integer getStartLine(){return startLine;} public Integer getEndLine(){return endLine;} public Instant getCreatedAt(){return createdAt;}
}
