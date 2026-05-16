package com.campus.ems.service;

import com.campus.ems.TestDataFactory;
import com.campus.ems.exception.BusinessException;
import com.campus.ems.model.Event;
import com.campus.ems.model.Registration;
import com.campus.ems.model.Student;
import com.campus.ems.repository.EventRepository;
import com.campus.ems.repository.RegistrationRepository;
import com.campus.ems.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private RegistrationService registrationService;

    // ---- registerStudentForEvent ----

    @Test
    void registerStudentForEvent_createsConfirmedRegistrationAndIncrementsCount() {
        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        student.setId(1L);
        Event event = TestDataFactory.makeEvent("Workshop", "UPCOMING", 50);
        event.setId(10L);
        event.setRegisteredCount(5);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByStudentIdAndEventId(1L, 10L)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Registration result = registrationService.registerStudentForEvent(1L, 10L);

        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(event.getRegisteredCount()).isEqualTo(6);
        verify(registrationRepository).save(any(Registration.class));
        verify(eventRepository).save(event);
    }

    @Test
    void registerStudentForEvent_throwsBusinessException_whenAlreadyRegistered() {
        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        student.setId(1L);
        Event event = TestDataFactory.makeEvent("Workshop", "UPCOMING", 50);
        event.setId(10L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByStudentIdAndEventId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.registerStudentForEvent(1L, 10L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void registerStudentForEvent_throwsBusinessException_whenEventAtFullCapacity() {
        Student student = TestDataFactory.makeStudent("Bob", "bob@campus.edu");
        student.setId(2L);
        Event event = TestDataFactory.makeEvent("Full Event", "UPCOMING", 10);
        event.setId(20L);
        event.setRegisteredCount(10); // full

        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(eventRepository.findById(20L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByStudentIdAndEventId(2L, 20L)).thenReturn(false);

        assertThatThrownBy(() -> registrationService.registerStudentForEvent(2L, 20L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void registerStudentForEvent_throwsBusinessException_whenEventStatusIsNotUpcoming() {
        Student student = TestDataFactory.makeStudent("Carol", "carol@campus.edu");
        student.setId(3L);
        Event event = TestDataFactory.makeEvent("Completed Event", "COMPLETED", 50);
        event.setId(30L);
        event.setRegisteredCount(0);

        when(studentRepository.findById(3L)).thenReturn(Optional.of(student));
        when(eventRepository.findById(30L)).thenReturn(Optional.of(event));
        when(registrationRepository.existsByStudentIdAndEventId(3L, 30L)).thenReturn(false);

        assertThatThrownBy(() -> registrationService.registerStudentForEvent(3L, 30L))
                .isInstanceOf(BusinessException.class);
    }

    // ---- cancelRegistration ----

    @Test
    void cancelRegistration_setsStatusCancelledAndDecrementsCount() {
        Event event = TestDataFactory.makeEvent("Workshop", "UPCOMING", 50);
        event.setId(10L);
        event.setRegisteredCount(5);

        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        Registration registration = TestDataFactory.makeRegistration(student, event);
        registration.setId(100L);
        registration.setStatus("CONFIRMED");

        when(registrationRepository.findById(100L)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        registrationService.cancelRegistration(100L);

        assertThat(registration.getStatus()).isEqualTo("CANCELLED");
        assertThat(event.getRegisteredCount()).isEqualTo(4);
        verify(eventRepository).save(event);
    }

    @Test
    void cancelRegistration_throwsBusinessException_whenAlreadyCancelled() {
        Event event = TestDataFactory.makeEvent("Workshop", "UPCOMING", 50);
        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        Registration registration = TestDataFactory.makeRegistration(student, event);
        registration.setId(101L);
        registration.setStatus("CANCELLED");

        when(registrationRepository.findById(101L)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.cancelRegistration(101L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void cancelRegistration_doesNotDecrementRegisteredCountBelowZero() {
        Event event = TestDataFactory.makeEvent("Workshop", "UPCOMING", 50);
        event.setId(10L);
        event.setRegisteredCount(0); // already at 0

        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        Registration registration = TestDataFactory.makeRegistration(student, event);
        registration.setId(102L);
        registration.setStatus("CONFIRMED");

        when(registrationRepository.findById(102L)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));

        registrationService.cancelRegistration(102L);

        assertThat(event.getRegisteredCount()).isEqualTo(0);
        // eventRepository.save should NOT be called since count is already 0
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ---- submitFeedback ----

    @Test
    void submitFeedback_throwsBusinessException_whenFeedbackAlreadySubmitted() {
        Event event = TestDataFactory.makeEvent("Workshop", "COMPLETED", 50);
        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        Registration registration = TestDataFactory.makeRegistration(student, event);
        registration.setId(200L);
        registration.setFeedbackRating(4); // already submitted

        when(registrationRepository.findById(200L)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.submitFeedback(200L, 5, "Great event!"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already submitted");
    }
}
