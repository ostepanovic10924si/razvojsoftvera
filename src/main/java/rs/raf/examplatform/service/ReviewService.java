package rs.raf.examplatform.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.examplatform.model.Assignment;
import rs.raf.examplatform.model.ExamSession;
import rs.raf.examplatform.model.ReviewComment;
import rs.raf.examplatform.model.Submission;
import rs.raf.examplatform.model.User;
import rs.raf.examplatform.repository.ReviewCommentRepository;
import rs.raf.examplatform.repository.SubmissionRepository;
import rs.raf.examplatform.repository.UserRepository;
import rs.raf.examplatform.utility.Messages;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {
    private static final int MAX_COMMENT_LENGTH = 4000;
    private static final int MAX_FILE_PATH_LENGTH = 255;

    private final SubmissionRepository submissions;
    private final ReviewCommentRepository comments;
    private final UserRepository users;

    public ReviewService(SubmissionRepository submissions, ReviewCommentRepository comments, UserRepository users) {
        this.submissions = submissions;
        this.comments = comments;
        this.users = users;
    }

    public record SubmissionSummary(Long submissionId, Long studentId, String username, String fullName,
                                    String studentIndex, Long assignmentId, String assignmentTitle, Long sessionId,
                                    String sessionName, String subjectCode, String examPeriodName, String groupName,
                                    String room, Instant downloadedAt, Instant submittedAt, String repositoryUrl,
                                    Double points) {}

    public record CommentView(Long id, String filePath, Integer startLine, Integer endLine, String body,
                              String teacherUsername, Instant createdAt) {}

    public record SubmissionDetail(SubmissionSummary submission, List<CommentView> comments) {}

    public record StudentResult(Long submissionId, Long assignmentId, String assignmentTitle, String sessionName,
                                String subjectName, Instant submittedAt, String repositoryUrl, Double points,
                                List<CommentView> comments) {}

    /**
     * Finds submitted solutions that match all given criteria; every criterion is optional.
     * The student criterion matches a username or a student index, ignoring case.
     */
    @Transactional(readOnly = true)
    public List<SubmissionSummary> search(Long subjectId, Long examPeriodId, Long sessionId, Long groupId,
                                          String room, String student) {
        return submissions.search(subjectId, examPeriodId, sessionId, groupId, blankToNull(room), blankToNull(student))
                .stream()
                .map(ReviewService::toSummary)
                .toList();
    }

    /**
     * Returns one submission with all of its review comments, ordered by file and line.
     */
    @Transactional(readOnly = true)
    public SubmissionDetail detail(Long submissionId) {
        Submission submission = findSubmission(submissionId);
        return new SubmissionDetail(toSummary(submission), commentsOf(submission));
    }

    /**
     * Adds a teacher's comment tied to a file and a line range of a submitted solution.
     * Without an end line the comment covers only the start line.
     */
    @Transactional
    public CommentView addComment(String teacherUsername, Long submissionId, String filePath, Integer startLine,
                                  Integer endLine, String body) {
        Submission submission = findSubmitted(submissionId);
        if (isBlank(body) || isBlank(filePath) || startLine == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.COMMENT_FIELDS_REQUIRED);
        }
        int lastLine = endLine == null ? startLine : endLine;
        if (startLine < 1 || lastLine < startLine) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.COMMENT_INVALID_LINES);
        }
        String text = body.trim();
        String path = filePath.trim();
        if (text.length() > MAX_COMMENT_LENGTH || path.length() > MAX_FILE_PATH_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.COMMENT_TOO_LONG);
        }
        User teacher = users.findByUsername(teacherUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, Messages.ACCOUNT_NOT_FOUND));
        return toCommentView(comments.save(new ReviewComment(submission, teacher, text, path, startLine, lastLine)));
    }

    /**
     * Sets or replaces the points of a submitted solution.
     */
    @Transactional
    public SubmissionSummary assignPoints(Long submissionId, Double points) {
        Submission submission = findSubmitted(submissionId);
        if (points == null || points < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Messages.POINTS_INVALID);
        }
        submission.assignPoints(points);
        return toSummary(submissions.save(submission));
    }

    /**
     * Returns the logged-in student's submitted solutions with points and comments.
     * Results are selected by the student from the token, so other students' data is never returned.
     */
    @Transactional(readOnly = true)
    public List<StudentResult> studentResults(String username) {
        User student = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, Messages.ACCOUNT_NOT_FOUND));
        return submissions.findAllByStudentIdAndSubmittedAtIsNotNullOrderBySubmittedAtDesc(student.getId()).stream()
                .map(submission -> toStudentResult(submission, commentsOf(submission)))
                .toList();
    }

    private Submission findSubmission(Long submissionId) {
        return submissions.findById(submissionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, Messages.SUBMISSION_NOT_FOUND));
    }

    private Submission findSubmitted(Long submissionId) {
        Submission submission = findSubmission(submissionId);
        if (submission.getSubmittedAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, Messages.SUBMISSION_NOT_SUBMITTED);
        }
        return submission;
    }

    private List<CommentView> commentsOf(Submission submission) {
        return comments.findAllBySubmissionIdOrderByFilePathAscStartLineAscIdAsc(submission.getId()).stream()
                .map(ReviewService::toCommentView)
                .toList();
    }

    private static SubmissionSummary toSummary(Submission submission) {
        User student = submission.getStudent();
        Assignment assignment = submission.getAssignment();
        ExamSession session = assignment.getSession();
        return new SubmissionSummary(
                submission.getId(),
                student.getId(),
                student.getUsername(),
                student.getFullName(),
                student.getStudentIndex(),
                assignment.getId(),
                assignment.getTitle(),
                session.getId(),
                session.getName(),
                session.getSubject().getCode(),
                session.getExamPeriod().getName(),
                session.getExamGroup().getName(),
                session.getExamGroup().getRoom(),
                submission.getDownloadedAt(),
                submission.getSubmittedAt(),
                submission.getRepositoryUrl(),
                submission.getPoints());
    }

    private static StudentResult toStudentResult(Submission submission, List<CommentView> comments) {
        Assignment assignment = submission.getAssignment();
        ExamSession session = assignment.getSession();
        return new StudentResult(
                submission.getId(),
                assignment.getId(),
                assignment.getTitle(),
                session.getName(),
                session.getSubject().getName(),
                submission.getSubmittedAt(),
                submission.getRepositoryUrl(),
                submission.getPoints(),
                comments);
    }

    private static CommentView toCommentView(ReviewComment comment) {
        return new CommentView(
                comment.getId(),
                comment.getFilePath(),
                comment.getStartLine(),
                comment.getEndLine(),
                comment.getBody(),
                comment.getTeacher().getUsername(),
                comment.getCreatedAt());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
