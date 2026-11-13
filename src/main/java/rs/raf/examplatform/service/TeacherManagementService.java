package rs.raf.examplatform.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.examplatform.model.Assignment;
import rs.raf.examplatform.model.ExamGroup;
import rs.raf.examplatform.model.ExamPeriod;
import rs.raf.examplatform.model.ExamRegistration;
import rs.raf.examplatform.model.ExamSession;
import rs.raf.examplatform.model.Role;
import rs.raf.examplatform.model.Subject;
import rs.raf.examplatform.model.User;
import rs.raf.examplatform.repository.AssignmentRepository;
import rs.raf.examplatform.repository.ExamGroupRepository;
import rs.raf.examplatform.repository.ExamPeriodRepository;
import rs.raf.examplatform.repository.ExamRegistrationRepository;
import rs.raf.examplatform.repository.ExamSessionRepository;
import rs.raf.examplatform.repository.SubjectRepository;
import rs.raf.examplatform.repository.UserRepository;
import rs.raf.examplatform.utility.Messages;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Teacher-side management of subjects, exam periods, groups, sessions, assignments,
 * student accounts and registration lists.
 */
@Service
public class TeacherManagementService {
    private final SubjectRepository subjects;
    private final ExamPeriodRepository periods;
    private final ExamGroupRepository groups;
    private final ExamSessionRepository sessions;
    private final AssignmentRepository assignments;
    private final UserRepository users;
    private final ExamRegistrationRepository registrations;
    private final PasswordEncoder passwordEncoder;

    public TeacherManagementService(SubjectRepository subjects, ExamPeriodRepository periods,
                                    ExamGroupRepository groups, ExamSessionRepository sessions,
                                    AssignmentRepository assignments, UserRepository users,
                                    ExamRegistrationRepository registrations, PasswordEncoder passwordEncoder) {
        this.subjects = subjects;
        this.periods = periods;
        this.groups = groups;
        this.sessions = sessions;
        this.assignments = assignments;
        this.users = users;
        this.registrations = registrations;
        this.passwordEncoder = passwordEncoder;
    }

    //region Subjects, exam periods and groups
    public Subject createSubject(String code, String name) {
        required(code, Messages.SUBJECT_CODE_REQUIRED);
        required(name, Messages.SUBJECT_NAME_REQUIRED);
        return subjects.save(new Subject(code.trim(), name.trim()));
    }

    public List<Subject> listSubjects() {
        return subjects.findAll();
    }

    public ExamPeriod createPeriod(String name, LocalDate startsOn, LocalDate endsOn) {
        required(name, Messages.PERIOD_NAME_REQUIRED);
        if (startsOn != null && endsOn != null && endsOn.isBefore(startsOn)) {
            throw badRequest(Messages.PERIOD_END_BEFORE_START);
        }
        return periods.save(new ExamPeriod(name.trim(), startsOn, endsOn));
    }

    public List<ExamPeriod> listPeriods() {
        return periods.findAll();
    }

    public ExamGroup createGroup(String name, String room) {
        required(name, Messages.GROUP_NAME_REQUIRED);
        return groups.save(new ExamGroup(name.trim(), room));
    }

    public List<ExamGroup> listGroups() {
        return groups.findAll();
    }
    //endregion

    //region Sessions and assignments

    /**
     * Creates an exam session for an existing subject, exam period and group. The end must be after the start.
     */
    public ExamSession createSession(String name, Instant startsAt, Instant endsAt,
                                     Long subjectId, Long examPeriodId, Long groupId) {
        required(name, Messages.SESSION_NAME_REQUIRED);
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw badRequest(Messages.SESSION_INVALID_TIME_RANGE);
        }
        if (subjectId == null || examPeriodId == null || groupId == null) {
            throw badRequest(Messages.SESSION_REFERENCES_REQUIRED);
        }
        Subject subject = subjects.findById(subjectId).orElseThrow(() -> notFound(Messages.SUBJECT_NOT_FOUND));
        ExamPeriod period = periods.findById(examPeriodId).orElseThrow(() -> notFound(Messages.PERIOD_NOT_FOUND));
        ExamGroup group = groups.findById(groupId).orElseThrow(() -> notFound(Messages.GROUP_NOT_FOUND));
        return sessions.save(new ExamSession(name.trim(), startsAt, endsAt, subject, period, group));
    }

    public List<ExamSession> listSessions() {
        return sessions.findAll();
    }

    public Assignment createAssignment(Long sessionId, String title, String description, String projectUrl) {
        required(title, Messages.ASSIGNMENT_TITLE_REQUIRED);
        required(projectUrl, Messages.ASSIGNMENT_PROJECT_URL_REQUIRED);
        ExamSession session = sessions.findById(sessionId).orElseThrow(() -> notFound(Messages.SESSION_NOT_FOUND));
        return assignments.save(new Assignment(title.trim(), description, projectUrl.trim(), session));
    }

    public List<Assignment> listAssignments(Long sessionId) {
        requireSessionExists(sessionId);
        return assignments.findAllBySessionIdOrderByIdAsc(sessionId);
    }
    //endregion

    //region Students and registration lists

    /**
     * Creates a student account with a hashed password. Usernames must be unique.
     */
    public User createStudent(String username, String password, String fullName, String studentIndex) {
        required(username, Messages.USERNAME_REQUIRED);
        required(password, Messages.PASSWORD_REQUIRED);
        required(studentIndex, Messages.STUDENT_INDEX_REQUIRED);
        if (users.findByUsername(username.trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.USERNAME_TAKEN);
        }
        return users.save(new User(username.trim(), passwordEncoder.encode(password), Role.STUDENT,
                fullName, studentIndex.trim()));
    }

    public List<User> listStudents() {
        return users.findAllByRoleOrderByIdAsc(Role.STUDENT);
    }

    /**
     * Adds students to a session's registration list. The whole list is rejected if any username is blank,
     * repeated, unknown, not a student or already registered for the session.
     */
    @Transactional
    public List<ExamRegistration> registerStudents(Long sessionId, List<String> studentUsernames) {
        ExamSession session = sessions.findById(sessionId).orElseThrow(() -> notFound(Messages.SESSION_NOT_FOUND));
        if (studentUsernames == null || studentUsernames.isEmpty()) {
            throw badRequest(Messages.REGISTRATION_LIST_EMPTY);
        }
        if (studentUsernames.stream().anyMatch(username -> username == null || username.isBlank())
                || studentUsernames.stream().map(String::trim).distinct().count() != studentUsernames.size()) {
            throw badRequest(Messages.REGISTRATION_LIST_INVALID);
        }
        List<User> selected = studentUsernames.stream()
                .map(String::trim)
                .map(username -> users.findByUsername(username)
                        .orElseThrow(() -> notFound(Messages.STUDENT_NOT_FOUND.formatted(username))))
                .toList();
        if (selected.stream().anyMatch(user -> user.getRole() != Role.STUDENT)) {
            throw badRequest(Messages.REGISTRATION_LIST_NOT_STUDENTS);
        }
        for (User student : selected) {
            if (registrations.existsByStudentIdAndExamSessionId(student.getId(), sessionId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        Messages.STUDENT_ALREADY_REGISTERED.formatted(student.getUsername()));
            }
        }
        return selected.stream()
                .map(student -> registrations.save(new ExamRegistration(student, session)))
                .toList();
    }

    public List<ExamRegistration> listRegistrations(Long sessionId) {
        requireSessionExists(sessionId);
        return registrations.findAllByExamSessionIdOrderByIdAsc(sessionId);
    }
    //endregion

    private void requireSessionExists(Long sessionId) {
        if (!sessions.existsById(sessionId)) {
            throw notFound(Messages.SESSION_NOT_FOUND);
        }
    }

    private static void required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw badRequest(message);
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
