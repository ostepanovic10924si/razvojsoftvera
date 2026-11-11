package rs.raf.examplatform.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.raf.examplatform.service.MonitoringService;
import rs.raf.examplatform.service.MonitoringService.SessionMonitoring;

@RestController
@RequestMapping("/api/teacher")
public class TeacherMonitoringController {
    private final MonitoringService monitoring;

    public TeacherMonitoringController(MonitoringService monitoring) {
        this.monitoring = monitoring;
    }

    @GetMapping("/sessions/{sessionId}/monitoring")
    public SessionMonitoring sessionMonitoring(@PathVariable Long sessionId) {
        return monitoring.monitor(sessionId);
    }
}
