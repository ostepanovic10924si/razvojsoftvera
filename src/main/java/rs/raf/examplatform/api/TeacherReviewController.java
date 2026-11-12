package rs.raf.examplatform.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import rs.raf.examplatform.service.ReviewService;
import rs.raf.examplatform.service.ReviewService.CommentView;
import rs.raf.examplatform.service.ReviewService.SubmissionDetail;
import rs.raf.examplatform.service.ReviewService.SubmissionSummary;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherReviewController {
    private final ReviewService reviews;

    public TeacherReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    public record CommentRequest(String filePath, Integer startLine, Integer endLine, String body) {}

    public record PointsRequest(Double points) {}

    @GetMapping("/submissions")
    public List<SubmissionSummary> submissions(@RequestParam(required = false) Long subjectId,
                                               @RequestParam(required = false) Long examPeriodId,
                                               @RequestParam(required = false) Long sessionId,
                                               @RequestParam(required = false) Long groupId,
                                               @RequestParam(required = false) String room,
                                               @RequestParam(required = false) String student) {
        return reviews.search(subjectId, examPeriodId, sessionId, groupId, room, student);
    }

    @GetMapping("/submissions/{submissionId}")
    public SubmissionDetail submission(@PathVariable Long submissionId) {
        return reviews.detail(submissionId);
    }

    @PostMapping("/submissions/{submissionId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentView addComment(@PathVariable Long submissionId, @RequestBody CommentRequest request,
                                  Authentication auth) {
        return reviews.addComment(auth.getName(), submissionId, request.filePath(), request.startLine(),
                request.endLine(), request.body());
    }

    @PutMapping("/submissions/{submissionId}/points")
    public SubmissionSummary assignPoints(@PathVariable Long submissionId, @RequestBody PointsRequest request) {
        return reviews.assignPoints(submissionId, request.points());
    }
}
