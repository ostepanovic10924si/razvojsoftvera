package rs.raf.examplatform.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import rs.raf.examplatform.model.*;
import rs.raf.examplatform.service.TeacherManagementService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherManagementController {

    private final TeacherManagementService management;

    public TeacherManagementController(TeacherManagementService management) {
        this.management = management;
    }

    //region Records
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
    //endregion

    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectView createSubject(@RequestBody SubjectRequest request) {
        return subjectView(management.createSubject(request.code(), request.name()));
    }

    @GetMapping("/subjects")
    public List<SubjectView> listSubjects() {
        return management.listSubjects().stream().map(this::subjectView).toList();
    }

    @PostMapping("/exam-periods")
    @ResponseStatus(HttpStatus.CREATED)
    public PeriodView createPeriod(@RequestBody PeriodRequest request) {
        return periodView(management.createPeriod(request.name(), request.startsOn(), request.endsOn()));
    }

    @GetMapping("/exam-periods")
    public List<PeriodView> listPeriods() {
        return management.listPeriods().stream().map(this::periodView).toList();
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupView createGroup(@RequestBody GroupRequest request) {
        return groupView(management.createGroup(request.name(), request.room()));
    }

    @GetMapping("/groups")
    public List<GroupView> listGroups() {
        return management.listGroups().stream().map(this::groupView).toList();
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionView createSession(@RequestBody SessionRequest request) {
        return sessionView(management.createSession(request.name(), request.startsAt(), request.endsAt(),
                request.subjectId(), request.examPeriodId(), request.groupId()));
    }

    @GetMapping("/sessions")
    public List<SessionView> listSessions() {
        return management.listSessions().stream().map(this::sessionView).toList();
    }

    @PostMapping("/sessions/{sessionId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentView createAssignment(@PathVariable Long sessionId, @RequestBody AssignmentRequest request) {
        return assignmentView(management.createAssignment(sessionId, request.title(), request.description(),
                request.projectUrl()));
    }

    @GetMapping("/sessions/{sessionId}/assignments")
    public List<AssignmentView> listAssignments(@PathVariable Long sessionId) {
        return management.listAssignments(sessionId).stream().map(this::assignmentView).toList();
    }

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentView createStudent(@RequestBody StudentRequest request) {
        return studentView(management.createStudent(request.username(), request.password(), request.fullName(),
                request.studentIndex()));
    }

    @GetMapping("/students")
    public List<StudentView> listStudents() {
        return management.listStudents().stream().map(this::studentView).toList();
    }

    @PostMapping("/sessions/{sessionId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    public List<RegistrationView> registerStudents(@PathVariable Long sessionId,
                                                   @RequestBody RegistrationRequest request) {
        return management.registerStudents(sessionId, request.studentUsernames()).stream()
                .map(this::registrationView)
                .toList();
    }

    @GetMapping("/sessions/{sessionId}/registrations")
    public List<RegistrationView> listRegistrations(@PathVariable Long sessionId) {
        return management.listRegistrations(sessionId).stream().map(this::registrationView).toList();
    }

    //region Mapping to views
    private SubjectView subjectView(Subject s) {
        return new SubjectView(s.getId(), s.getCode(), s.getName());
    }

    private PeriodView periodView(ExamPeriod p) {
        return new PeriodView(p.getId(), p.getName(), p.getStartsOn(), p.getEndsOn());
    }

    private GroupView groupView(ExamGroup g) {
        return new GroupView(g.getId(), g.getName(), g.getRoom());
    }

    private SessionView sessionView(ExamSession s) {
        return new SessionView(s.getId(), s.getName(), s.getStartsAt(), s.getEndsAt(), s.getSubject().getId(),
                s.getExamPeriod().getId(), s.getExamGroup().getId());
    }

    private AssignmentView assignmentView(Assignment a) {
        return new AssignmentView(a.getId(), a.getTitle(), a.getDescription(), a.getProjectUrl(),
                a.getSession().getId());
    }

    private StudentView studentView(User u) {
        return new StudentView(u.getId(), u.getUsername(), u.getFullName(), u.getStudentIndex());
    }

    private RegistrationView registrationView(ExamRegistration r) {
        return new RegistrationView(r.getId(), r.getStudent().getId(), r.getStudent().getUsername(),
                r.getStudent().getStudentIndex(), r.getExamSession().getId(), r.getRegisteredAt());
    }
    //endregion
}
