package rs.raf.examplatform.model;

import jakarta.persistence.*;

@Entity public class Assignment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String title;
    @Column(length=2000) private String description;
    @Column(nullable=false) private String projectUrl;
    @ManyToOne(optional=false) private ExamSession session;
    protected Assignment() {}
    public Assignment(String title,String description,String projectUrl,ExamSession session){this.title=title;this.description=description;this.projectUrl=projectUrl;this.session=session;}
    public Long getId(){return id;} public String getTitle(){return title;} public String getDescription(){return description;} public String getProjectUrl(){return projectUrl;} public ExamSession getSession(){return session;}
}
