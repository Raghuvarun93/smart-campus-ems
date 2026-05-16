package com.campus.ems.service;

import com.campus.ems.exception.BusinessException;
import com.campus.ems.exception.ResourceNotFoundException;
import com.campus.ems.model.Event;
import com.campus.ems.model.Registration;
import com.campus.ems.model.Student;
import com.campus.ems.repository.EventRepository;
import com.campus.ems.repository.RegistrationRepository;
import com.campus.ems.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EmailService emailService;

    // ── Register ──────────────────────────────────────────────────────────────

    public Registration registerStudentForEvent(Long studentId, Long eventId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (registrationRepository.existsByStudentIdAndEventId(studentId, eventId)) {
            throw new BusinessException("You are already registered for this event!");
        }

        if (event.isFull()) {
            throw new BusinessException("This event has reached maximum capacity.");
        }

        if (!"UPCOMING".equals(event.getStatus())) {
            throw new BusinessException("Registrations are only open for UPCOMING events.");
        }

        Registration registration = new Registration();
        registration.setStudent(student);
        registration.setEvent(event);
        registration.setStatus("CONFIRMED");
        registrationRepository.save(registration);

        event.setRegisteredCount(event.getRegisteredCount() + 1);
        eventRepository.save(event);

        // Send confirmation email (async — won't block the response)
        emailService.sendRegistrationConfirmation(student, event);

        return registration;
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    public void cancelRegistration(Long registrationId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + registrationId));

        if ("CANCELLED".equals(registration.getStatus())) {
            throw new BusinessException("This registration is already cancelled.");
        }

        Student student = registration.getStudent();
        Event event = registration.getEvent();

        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);

        if (event.getRegisteredCount() > 0) {
            event.setRegisteredCount(event.getRegisteredCount() - 1);
            eventRepository.save(event);
        }

        // Send cancellation email (async)
        emailService.sendCancellationEmail(student, event);
    }

    // ── Feedback ──────────────────────────────────────────────────────────────

    public Registration submitFeedback(Long registrationId, int rating, String comment) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));

        if (registration.getFeedbackRating() != null) {
            throw new BusinessException("You have already submitted feedback for this event.");
        }

        registration.setFeedbackRating(rating);
        registration.setFeedbackComment(comment);
        registration.setFeedbackSubmittedAt(LocalDateTime.now());
        return registrationRepository.save(registration);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Registration> getRegistrationsByStudent(Long studentId) {
        return registrationRepository.findRegistrationsWithEventsByStudentId(studentId);
    }

    public List<Registration> getRegistrationsByEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    public boolean isStudentRegistered(Long studentId, Long eventId) {
        return registrationRepository.existsByStudentIdAndEventId(studentId, eventId);
    }

    public Map<Long, Long> getRegistrationCountPerStudent() {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : registrationRepository.countRegistrationsPerStudent()) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }
}
