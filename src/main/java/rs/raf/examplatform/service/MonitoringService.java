package rs.raf.examplatform.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.examplatform.model.ExamRegistration;
import rs.raf.examplatform.model.ExamSession;
import rs.raf.examplatform.model.Submission;
import rs.raf.examplatform.model.User;
import rs.raf.examplatform.repository.AssignmentRepository;
import rs.raf.examplatform.repository.ExamRegistrationRepository;
import rs.raf.examplatform.repository.ExamSessionRepository;
import rs.raf.examplatform.repository.SubmissionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
public class MonitoringService {
    private final ExamSessionRepository sessions;
    private final ExamRegistrationRepository registrations;
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;

    public MonitoringService(ExamSessionRepository sessions, ExamRegistrationRepository registrations,
                             AssignmentRepository assignments, SubmissionRepository submissions) {
        this.sessions = sessions;
        this.registrations = registrations;
        this.assignments = assignments;
        this.submissions = submissions;
    }

    public enum StudentStatus { NOT_DOWNLOADED, IN_PROGRESS, SUBMITTED }

    public record StudentProgress(Long studentId, String username, String fullName, String studentIndex,
                                  StudentStatus status, Instant downloadedAt, Instant submittedAt,
                                  Long workMinutes) {}

    public record SessionMonitoring(Long sessionId, String sessionName, Instant startsAt, Instant endsAt,
                                    Instant serverTime, int registered, int downloaded, int inProgress,
                                    int submitted, Double averageWorkMinutes, List<StudentProgress> students) {}

    /**
     * Builds monitoring data for an exam session: the status of every registered student, their download and
     * submission times, and summary counts with the average working time of submitted works.
     */
    @Transactional(readOnly = true)
    public SessionMonitoring monitor(Long sessionId) {
        ExamSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termin nije pronadjen"));
        int assignmentCount = assignments.findAllBySessionIdOrderByIdAsc(sessionId).size();
        Map<Long, List<Submission>> worksByStudent = submissions.findAllByAssignmentSessionId(sessionId).stream()
                .collect(Collectors.groupingBy(work -> work.getStudent().getId()));
        List<StudentProgress> students = registrations.findAllByExamSessionIdOrderByIdAsc(sessionId).stream()
                .map(ExamRegistration::getStudent)
                .map(student -> progress(student, worksByStudent.getOrDefault(student.getId(), List.of()),
                        assignmentCount))
                .toList();
        OptionalDouble averageSeconds = students.stream()
                .filter(student -> student.status() == StudentStatus.SUBMITTED)
                .mapToLong(student -> Duration.between(student.downloadedAt(), student.submittedAt()).getSeconds())
                .average();
        Double averageWorkMinutes = averageSeconds.isPresent()
                ? Math.round(averageSeconds.getAsDouble() / 6.0) / 10.0
                : null;
        int inProgress = count(students, StudentStatus.IN_PROGRESS);
        int submitted = count(students, StudentStatus.SUBMITTED);
        return new SessionMonitoring(
                session.getId(),
                session.getName(),
                session.getStartsAt(),
                session.getEndsAt(),
                Instant.now(),
                students.size(),
                inProgress + submitted,
                inProgress,
                submitted,
                averageWorkMinutes,
                students);
    }

    private static StudentProgress progress(User student, List<Submission> works, int assignmentCount) {
        Instant downloadedAt = works.stream()
                .map(Submission::getDownloadedAt)
                .min(Comparator.naturalOrder())
                .orElse(null);
        boolean allSubmitted = assignmentCount > 0 && works.size() == assignmentCount
                && works.stream().allMatch(work -> work.getSubmittedAt() != null);
        Instant submittedAt = allSubmitted
                ? works.stream().map(Submission::getSubmittedAt).max(Comparator.naturalOrder()).orElse(null)
                : null;
        StudentStatus status = downloadedAt == null ? StudentStatus.NOT_DOWNLOADED
                : allSubmitted ? StudentStatus.SUBMITTED
                : StudentStatus.IN_PROGRESS;
        Long workMinutes = submittedAt == null ? null : Duration.between(downloadedAt, submittedAt).toMinutes();
        return new StudentProgress(student.getId(), student.getUsername(), student.getFullName(),
                student.getStudentIndex(), status, downloadedAt, submittedAt, workMinutes);
    }

    private static int count(List<StudentProgress> students, StudentStatus status) {
        return (int) students.stream().filter(student -> student.status() == status).count();
    }
}
