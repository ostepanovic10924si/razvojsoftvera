package rs.raf.examplatform.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.raf.examplatform.model.Assignment;
import rs.raf.examplatform.model.ExamSession;
import rs.raf.examplatform.model.Submission;
import rs.raf.examplatform.service.ReviewService;
import rs.raf.examplatform.service.ReviewService.StudentResult;
import rs.raf.examplatform.service.StudentExamService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentExamController {
    private final StudentExamService studentExams;
    private final ReviewService reviews;

    public StudentExamController(StudentExamService studentExams, ReviewService reviews) {
        this.studentExams = studentExams;
        this.reviews = reviews;
    }

    public record SessionOption(Long id, String name, String subjectName, Long groupId,
                                String groupName, String room, Instant startsAt, Instant endsAt) {}

    public record CheckInRequest(Long sessionId, Long groupId, String studentIndex) {}

    public record CheckInResponse(Long sessionId, String sessionName, String subjectName, String groupName,
                                  String room, Instant startsAt, Instant endsAt, Instant serverTime, String status) {}

    public record DownloadedAssignment(Long assignmentId, String title, String description,
                                       String projectUrl, Instant downloadedAt) {}

    public record SubmissionRequest(String repositoryUrl) {}

    public record SubmissionConfirmation(Long submissionId, Long assignmentId, String repositoryUrl,
                                         Instant downloadedAt, Instant submittedAt) {}

    @GetMapping("/sessions")
    public List<SessionOption> sessions() {
        List<ExamSession> openSessions = studentExams.openSessions();
        return openSessions.stream()
                .map(this::toSessionOption)
                .toList();
    }

    @GetMapping("/submissions")
    public List<StudentResult> results(Authentication auth) {
        return reviews.studentResults(auth.getName());
    }

    @PostMapping("/check-in")
    public CheckInResponse checkIn(@RequestBody CheckInRequest request, Authentication auth) {
        ExamSession session = studentExams.checkIn(auth.getName(), request.sessionId(), request.groupId(),
                request.studentIndex());
        Instant now = Instant.now();
        String status = now.isBefore(session.getStartsAt()) ? "NOT_STARTED" : "IN_PROGRESS";
        return new CheckInResponse(
                session.getId(),
                session.getName(),
                session.getSubject().getName(),
                session.getExamGroup().getName(),
                session.getExamGroup().getRoom(),
                session.getStartsAt(),
                session.getEndsAt(),
                now,
                status);
    }

    @PostMapping("/sessions/{sessionId}/download")
    public List<DownloadedAssignment> download(@PathVariable Long sessionId, Authentication auth) {
        return studentExams.download(auth.getName(), sessionId).stream()
                .map(this::toDownloadedAssignment)
                .toList();
    }

    @PostMapping("/assignments/{assignmentId}/submission")
    public SubmissionConfirmation submit(@PathVariable Long assignmentId, @RequestBody SubmissionRequest request,
                                         Authentication auth) {
        Submission submission = studentExams.submit(auth.getName(), assignmentId, request.repositoryUrl());
        return new SubmissionConfirmation(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getRepositoryUrl(),
                submission.getDownloadedAt(),
                submission.getSubmittedAt());
    }

    private DownloadedAssignment toDownloadedAssignment(Submission submission) {
        Assignment assignment = submission.getAssignment();
        return new DownloadedAssignment(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getProjectUrl(),
                submission.getDownloadedAt());
    }

    private SessionOption toSessionOption(ExamSession session) {
        return new SessionOption(
                session.getId(),
                session.getName(),
                session.getSubject().getName(),
                session.getExamGroup().getId(),
                session.getExamGroup().getName(),
                session.getExamGroup().getRoom(),
                session.getStartsAt(),
                session.getEndsAt());
    }
}
