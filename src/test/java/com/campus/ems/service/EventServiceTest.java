package com.campus.ems.service;

import com.campus.ems.TestDataFactory;
import com.campus.ems.exception.ResourceNotFoundException;
import com.campus.ems.model.Event;
import com.campus.ems.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getUpcomingEvents_returnsOnlyUpcomingEventsOrderedByDate() {
        Event e1 = TestDataFactory.makeEvent("Event A", "UPCOMING", 50);
        e1.setEventDate(LocalDate.now().plusDays(1));
        Event e2 = TestDataFactory.makeEvent("Event B", "UPCOMING", 50);
        e2.setEventDate(LocalDate.now().plusDays(5));

        when(eventRepository.findByStatusOrderByEventDateAsc("UPCOMING"))
                .thenReturn(List.of(e1, e2));

        List<Event> result = eventService.getUpcomingEvents();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Event A");
        assertThat(result.get(1).getTitle()).isEqualTo("Event B");
        assertThat(result).allMatch(e -> "UPCOMING".equals(e.getStatus()));
    }

    @Test
    void saveEvent_setsStatusToUpcoming_whenStatusIsNull() {
        Event event = TestDataFactory.makeEvent("New Event", null, 50);
        event.setStatus(null);
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.saveEvent(event);

        assertThat(result.getStatus()).isEqualTo("UPCOMING");
    }

    @Test
    void saveEvent_setsStatusToUpcoming_whenStatusIsBlank() {
        Event event = TestDataFactory.makeEvent("New Event", "   ", 50);
        event.setStatus("   ");
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        Event result = eventService.saveEvent(event);

        assertThat(result.getStatus()).isEqualTo("UPCOMING");
    }

    @Test
    void getEventById_throwsResourceNotFoundException_forNonExistentId() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getStatistics_returnsMapWithExpectedKeys() {
        when(eventRepository.count()).thenReturn(10L);
        when(eventRepository.totalRegistrations()).thenReturn(50L);
        when(eventRepository.findByStatusOrderByEventDateAsc("UPCOMING")).thenReturn(Collections.emptyList());
        when(eventRepository.countByEventType()).thenReturn(Collections.emptyList());
        when(eventRepository.countByDepartment()).thenReturn(Collections.emptyList());
        when(eventRepository.countByStatus()).thenReturn(Collections.emptyList());
        when(eventRepository.findTopEventsByRegistration()).thenReturn(Collections.emptyList());

        Map<String, Object> stats = eventService.getStatistics();

        assertThat(stats).containsKeys(
                "totalEvents",
                "totalRegistrations",
                "upcomingCount",
                "countByType",
                "countByDepartment",
                "countByStatus",
                "topEvents"
        );
    }
}
