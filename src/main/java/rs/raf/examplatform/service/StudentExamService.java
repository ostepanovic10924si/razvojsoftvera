package rs.raf.examplatform.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rs.raf.examplatform.model.Assignment;
import rs.raf.examplatform.model.ExamSession;
import rs.raf.examplatform.model.Submission;
import rs.raf.examplatform.model.User;
import rs.raf.examplatform.repository.AssignmentRepository;
import rs.raf.examplatform.repository.ExamRegistrationRepository;
import rs.raf.examplatform.repository.ExamSessionRepository;
import rs.raf.examplatform.repository.SubmissionRepository;
import rs.raf.examplatform.repository.UserRepository;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;

@Service
public class StudentExamService {
    private final ExamSessionRepository sessions;
    private final ExamRegistrationRepository registrations;
    private final UserRepository users;
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;

    public StudentExamService(ExamSessionRepository sessions, ExamRegistrationRepository registrations,
                              UserRepository users, AssignmentRepository assignments,
                              SubmissionRepository submissions) {
        this.sessions = sessions;
        this.registrations = registrations;
        this.users = users;
        this.assignments = assignments;
        this.submissions = submissions;
    }

    public List<ExamSession> openSessions(){
        return sessions.findAllByEndsAtAfterOrderByStartsAtAsc(Instant.now());
    }

    /**
     * Checks whether the logged-in student may take the selected exam session. Writes nothing to the database.
     */
    public ExamSession checkIn(String username, Long sessionId, Long groupId, String studentIndex) {
        if (sessionId == null || groupId == null || studentIndex == null || studentIndex.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Termin, grupa i broj indeksa su obavezni");
        }
        ExamSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termin nije pronadjen"));
        if (!session.getExamGroup().getId().equals(groupId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izabrana grupa ne polaze u ovom terminu");
        }
        if (!session.getEndsAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Termin je zavrsen");
        }
        User student = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nalog nije pronadjen"));
        if (student.getStudentIndex() == null
                || !student.getStudentIndex().trim().equalsIgnoreCase(studentIndex.trim())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Broj indeksa ne odgovara prijavljenom nalogu");
        }
        requireRegistered(student, session);
        return session;
    }

    /**
     * Returns the session's assignments to a registered student, only while the session is in progress.
     * Records the download time on the first download of each assignment; repeated downloads keep it unchanged.
     */
    @Transactional
    public List<Submission> download(String username, Long sessionId) {
        ExamSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termin nije pronadjen"));
        User student = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nalog nije pronadjen"));
        requireRegistered(student, session);
        Instant now = Instant.now();
        if (now.isBefore(session.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Termin jos nije poceo");
        }
        if (!now.isBefore(session.getEndsAt())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Termin je zavrsen");
        }
        List<Assignment> sessionAssignments = assignments.findAllBySessionIdOrderByIdAsc(session.getId());
        if (sessionAssignments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Za ovaj termin jos nema zadatka");
        }
        return sessionAssignments.stream()
                .map(assignment -> submissions.findByStudentIdAndAssignmentId(student.getId(), assignment.getId())
                        .orElseGet(() -> submissions.save(new Submission(student, assignment, now, null))))
                .toList();
    }

    /**
     * Accepts a solution as a repository URL, only while the session is in progress and only for a downloaded
     * assignment. Resubmitting before the session ends replaces the URL and the submission time.
     */
    @Transactional
    public Submission submit(String username, Long assignmentId, String repositoryUrl) {
        String url = requireValidUrl(repositoryUrl);
        Assignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zadatak nije pronadjen"));
        ExamSession session = assignment.getSession();
        User student = users.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nalog nije pronadjen"));
        requireRegistered(student, session);
        Instant now = Instant.now();
        if (now.isBefore(session.getStartsAt())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Termin jos nije poceo");
        }
        if (!now.isBefore(session.getEndsAt())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Termin je zavrsen, predaja vise nije moguca");
        }
        Submission submission = submissions.findByStudentIdAndAssignmentId(student.getId(), assignment.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Zadatak prvo mora da se preuzme"));
        submission.markSubmitted(url, now);
        return submissions.save(submission);
    }

    private static String requireValidUrl(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL repozitorijuma je obavezan");
        }
        String url = repositoryUrl.trim();
        if (url.length() > 2000 || !isHttpUrl(url)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "URL repozitorijuma nije ispravan");
        }
        return url;
    }

    private static boolean isHttpUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void requireRegistered(User student, ExamSession session) {
        if (!registrations.existsByStudentIdAndExamSessionId(student.getId(), session.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niste na spisku prijavljenih za ovaj termin");
        }
    }
}
