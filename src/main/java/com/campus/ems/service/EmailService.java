package com.campus.ems.service;

import com.campus.ems.model.Event;
import com.campus.ems.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email service — currently logs to console.
 * To enable real email sending:
 *   1. Uncomment spring-boot-starter-mail in pom.xml
 *   2. Set spring.mail.username and spring.mail.password in application.properties
 *   3. Set app.mail.enabled=true
 */
@Service
public class EmailService {

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public void sendRegistrationConfirmation(Student student, Event event) {
        String body = buildRegistrationEmail(student, event);
        if (mailEnabled) {
            // Real sending would go here once mail starter is added
            System.out.println("[EMAIL] Sending to: " + student.getEmail());
            System.out.println(body);
        } else {
            // Console log so you can see what would be sent
            System.out.println("\n========== REGISTRATION EMAIL (console mode) ==========");
            System.out.println("TO: " + student.getEmail());
            System.out.println(body);
            System.out.println("=======================================================\n");
        }
    }

    public void sendCancellationEmail(Student student, Event event) {
        String body = buildCancellationEmail(student, event);
        if (mailEnabled) {
            System.out.println("[EMAIL] Sending cancellation to: " + student.getEmail());
            System.out.println(body);
        } else {
            System.out.println("\n========== CANCELLATION EMAIL (console mode) ==========");
            System.out.println("TO: " + student.getEmail());
            System.out.println(body);
            System.out.println("=======================================================\n");
        }
    }

    private String buildRegistrationEmail(Student student, Event event) {
        return """
                Dear %s,

                Your registration has been CONFIRMED!

                ─────────────────────────────────────
                Event    : %s
                Type     : %s
                Date     : %s
                Time     : %s
                Venue    : %s
                Department: %s
                Organizer: %s
                ─────────────────────────────────────

                Please arrive 10 minutes before the event starts.

                View your events: http://localhost:8080/student/my-events

                Regards,
                Campus EMS Team
                """.formatted(
                student.getName(),
                event.getTitle(),
                event.getEventType(),
                event.getEventDate(),
                event.getStartTime() != null ? event.getStartTime() : "TBD",
                event.getVenue(),
                event.getDepartment(),
                event.getOrganizer()
        );
    }

    private String buildCancellationEmail(Student student, Event event) {
        return """
                Dear %s,

                Your registration for "%s" has been CANCELLED.

                ─────────────────────────────────────
                Event : %s
                Date  : %s
                Venue : %s
                ─────────────────────────────────────

                You can re-register at: http://localhost:8080/events/%d

                Regards,
                Campus EMS Team
                """.formatted(
                student.getName(),
                event.getTitle(),
                event.getTitle(),
                event.getEventDate(),
                event.getVenue(),
                event.getId()
        );
    }
}
