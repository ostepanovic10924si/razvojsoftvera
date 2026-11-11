package rs.raf.examplatform.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.examplatform.model.*;
import rs.raf.examplatform.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherManagementController {

    private final SubjectRepository subjects;
    private final ExamPeriodRepository periods;
    private final ExamGroupRepository groups;
    private final ExamSessionRepository sessions;
    private final AssignmentRepository assignments;
    private final UserRepository users;
    private final ExamRegistrationRepository registrations;
    private final PasswordEncoder passwordEncoder;

    public TeacherManagementController(SubjectRepository subjects, ExamPeriodRepository periods,
                                       ExamGroupRepository groups, ExamSessionRepository sessions, AssignmentRepository assignments,
                                       UserRepository users, ExamRegistrationRepository registrations, PasswordEncoder passwordEncoder) {
        this.subjects = subjects;
        this.periods = periods;
        this.groups = groups;
        this.sessions = sessions;
        this.assignments = assignments;
        this.users = users;
        this.registrations = registrations;
        this.passwordEncoder = passwordEncoder;
    }

    public record SubjectRequest(String code, String name) {
    }

    public record PeriodRequest(String name, LocalDate startsOn, LocalDate endsOn) {
    }

    public record GroupRequest(String name, String room) {
    }

    public record SessionRequest(String name, Instant startsAt, Instant endsAt,
                                 Long subjectId, Long examPeriodId, Long groupId) {
    }

    public record AssignmentRequest(String title, String description, String projectUrl) {
    }

    public record StudentRequest(String username, String password, String fullName, String studentIndex) {
    }

    public record RegistrationRequest(List<String> studentUsernames) {
    }

    public record SubjectView(Long id, String code, String name) {
    }

    public record PeriodView(Long id, String name, LocalDate startsOn, LocalDate endsOn) {
    }

    public record GroupView(Long id, String name, String room) {
    }

    public record SessionView(Long id, String name, Instant startsAt, Instant endsAt,
                              Long subjectId, Long examPeriodId, Long groupId) {
    }

    public record AssignmentView(Long id, String title, String description, String projectUrl, Long sessionId) {
    }

    public record StudentView(Long id, String username, String fullName, String studentIndex) {
    }

    public record RegistrationView(Long id, Long studentId, String username, String studentIndex,
                                   Long sessionId, Instant registeredAt) {
    }

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectView createSubject(@RequestBody SubjectRequest request) {
        required(request.code(), "Šifra predmeta je obavezna");
        required(request.name(), "Naziv predmeta je obavezan");
        Subject saved = subjects.save(new Subject(request.code().trim(), request.name().trim()));
        return new SubjectView(saved.getId(), saved.getCode(), saved.getName());
    }

    @GetMapping("/subjects")
    public List<SubjectView> listSubjects() {
        return subjects.findAll().stream().map(s -> new SubjectView(s.getId(), s.getCode(), s.getName())).toList();
    }

    @PostMapping("/exam-periods")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodView createPeriod(@RequestBody PeriodRequest request) {
        required(request.name(), "Naziv ispitnog roka je obavezan");
        if (request.startsOn() != null && request.endsOn() != null && request.endsOn().isBefore(request.startsOn()))
            throw badRequest("Kraj ispitnog roka ne može biti pre početka");
        ExamPeriod saved = periods.save(new ExamPeriod(request.name().trim(), request.startsOn(), request.endsOn()));
        return new PeriodView(saved.getId(), saved.getName(), saved.getStartsOn(), saved.getEndsOn());
    }

    @GetMapping("/exam-periods")
    public List<PeriodView> listPeriods() {
        return periods.findAll().stream().map(p -> new PeriodView(p.getId(), p.getName(), p.getStartsOn(), p.getEndsOn())).toList();
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupView createGroup(@RequestBody GroupRequest request) {
        required(request.name(), "Naziv grupe je obavezan");
        ExamGroup saved = groups.save(new ExamGroup(request.name().trim(), request.room()));
        return new GroupView(saved.getId(), saved.getName(), saved.getRoom());
    }

    @GetMapping("/groups")
    public List<GroupView> listGroups() {
        return groups.findAll().stream().map(g -> new GroupView(g.getId(), g.getName(), g.getRoom())).toList();
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionView createSession(@RequestBody SessionRequest request) {
        required(request.name(), "Naziv termina je obavezan");
        if (request.startsAt() == null || request.endsAt() == null || !request.endsAt().isAfter(request.startsAt()))
            throw badRequest("Termin mora imati validan početak i kraj");
        if (request.subjectId() == null || request.examPeriodId() == null || request.groupId() == null)
            throw badRequest("Predmet, ispitni rok i grupa su obavezni");
        Subject subject = subjects.findById(request.subjectId()).orElseThrow(() -> notFound("Predmet nije pronađen"));
        ExamPeriod period = periods.findById(request.examPeriodId()).orElseThrow(() -> notFound("Ispitni rok nije pronađen"));
        ExamGroup group = groups.findById(request.groupId()).orElseThrow(() -> notFound("Grupa nije pronađena"));
        ExamSession saved = sessions.save(new ExamSession(request.name().trim(), request.startsAt(), request.endsAt(), subject, period, group));
        return sessionView(saved);
    }

    @GetMapping("/sessions")
    public List<SessionView> listSessions() {
        return sessions.findAll().stream().map(this::sessionView).toList();
    }

    @PostMapping("/sessions/{sessionId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentView createAssignment(@PathVariable Long sessionId, @RequestBody AssignmentRequest request) {
        required(request.title(), "Naziv zadatka je obavezan");
        required(request.projectUrl(), "URL projekta je obavezan");
        ExamSession session = sessions.findById(sessionId).orElseThrow(() -> notFound("Termin nije pronađen"));
        Assignment saved = assignments.save(new Assignment(request.title().trim(), request.description(), request.projectUrl().trim(), session));
        return assignmentView(saved);
    }

    @GetMapping("/sessions/{sessionId}/assignments")
    public List<AssignmentView> listAssignments(@PathVariable Long sessionId) {
        if (!sessions.existsById(sessionId)) throw notFound("Termin nije pronađen");
        return assignments.findAllBySessionIdOrderByIdAsc(sessionId).stream().map(this::assignmentView).toList();
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentView createStudent(@RequestBody StudentRequest request) {
        required(request.username(), "Korisničko ime je obavezno");
        required(request.password(), "Lozinka je obavezna");
        required(request.studentIndex(), "Broj indeksa je obavezan");
        if (users.findByUsername(request.username().trim()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Korisničko ime već postoji");
        User student = users.save(new User(request.username().trim(), passwordEncoder.encode(request.password()), Role.STUDENT,
                request.fullName(), request.studentIndex().trim()));
        return studentView(student);
    }

    @GetMapping("/students")
    public List<StudentView> listStudents() {
        return users.findAllByRoleOrderByIdAsc(Role.STUDENT).stream().map(this::studentView).toList();
    }

    @PostMapping("/sessions/{sessionId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public List<RegistrationView> registerStudents(@PathVariable Long sessionId, @RequestBody RegistrationRequest request) {
        ExamSession session = sessions.findById(sessionId).orElseThrow(() -> notFound("Termin nije pronađen"));
        if (request.studentUsernames() == null || request.studentUsernames().isEmpty())
            throw badRequest("Navedite bar jednog studenta");
        if (request.studentUsernames().stream().anyMatch(u -> u == null || u.isBlank()) ||
                request.studentUsernames().stream().map(String::trim).distinct().count() != request.studentUsernames().size())
            throw badRequest("Spisak sadrži prazno ili ponovljeno korisničko ime");
        List<User> selected = request.studentUsernames().stream().map(String::trim).map(username -> users.findByUsername(username)
                .orElseThrow(() -> notFound("Student nije pronađen: " + username))).toList();
        if (selected.stream().anyMatch(user -> user.getRole() != Role.STUDENT))
            throw badRequest("Spisak može sadržati samo studentske naloge");
        for (User student : selected) {
            if (registrations.existsByStudentIdAndExamSessionId(student.getId(), sessionId))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Student je već prijavljen: " + student.getUsername());
        }
        return selected.stream().map(student -> registrations.save(new ExamRegistration(student, session)))
                .map(this::registrationView).toList();
    }

    @GetMapping("/sessions/{sessionId}/registrations")
    public List<RegistrationView> listRegistrations(@PathVariable Long sessionId) {
        if (!sessions.existsById(sessionId)) throw notFound("Termin nije pronađen");
        return registrations.findAllByExamSessionIdOrderByIdAsc(sessionId).stream().map(this::registrationView).toList();
    }

    private SessionView sessionView(ExamSession s) {
        return new SessionView(s.getId(), s.getName(), s.getStartsAt(), s.getEndsAt(), s.getSubject().getId(), s.getExamPeriod().getId(), s.getExamGroup().getId());
    }

    private AssignmentView assignmentView(Assignment a) {
        return new AssignmentView(a.getId(), a.getTitle(), a.getDescription(), a.getProjectUrl(), a.getSession().getId());
    }

    private StudentView studentView(User u) {
        return new StudentView(u.getId(), u.getUsername(), u.getFullName(), u.getStudentIndex());
    }

    private RegistrationView registrationView(ExamRegistration r) {
        return new RegistrationView(r.getId(), r.getStudent().getId(), r.getStudent().getUsername(), r.getStudent().getStudentIndex(), r.getExamSession().getId(), r.getRegisteredAt());
    }

    private static void required(String value, String message) {
        if (value == null || value.isBlank()) throw badRequest(message);
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}