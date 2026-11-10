package rs.raf.examplatform.model;

import jakarta.persistence.*;

@Entity public class Subject {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String code;
    @Column(nullable=false) private String name;
    protected Subject() {}
    public Subject(String code,String name){this.code=code;this.name=name;}
    public Long getId(){return id;} public String getCode(){return code;} public String getName(){return name;}
}
