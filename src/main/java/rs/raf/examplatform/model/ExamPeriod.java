package rs.raf.examplatform.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class ExamPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    private LocalDate startsOn;
    private LocalDate endsOn;

    protected ExamPeriod() {
    }

    public ExamPeriod(String name, LocalDate startsOn, LocalDate endsOn) {
        this.name = name;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getStartsOn() {
        return startsOn;
    }

    public LocalDate getEndsOn() {
        return endsOn;
    }
}
