package rs.raf.examplatform.model;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"name", "room"}))
public class ExamGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private String room;

    protected ExamGroup() {
    }

    public ExamGroup(String name, String room) {
        this.name = name;
        this.room = room;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }
}
