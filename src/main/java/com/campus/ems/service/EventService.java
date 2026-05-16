package com.campus.ems.service;

import com.campus.ems.exception.ResourceNotFoundException;
import com.campus.ems.model.Event;
import com.campus.ems.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Transactional
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    // ---- CRUD Operations ----

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
    }

    public Event saveEvent(Event event) {
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            event.setStatus("UPCOMING");
        }
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event updatedEvent) {
        Event existing = getEventById(id);
        existing.setTitle(updatedEvent.getTitle());
        existing.setDescription(updatedEvent.getDescription());
        existing.setEventDate(updatedEvent.getEventDate());
        existing.setStartTime(updatedEvent.getStartTime());
        existing.setEndTime(updatedEvent.getEndTime());
        existing.setVenue(updatedEvent.getVenue());
        existing.setDepartment(updatedEvent.getDepartment());
        existing.setEventType(updatedEvent.getEventType());
        existing.setTotalCapacity(updatedEvent.getTotalCapacity());
        existing.setOrganizer(updatedEvent.getOrganizer());
        existing.setStatus(updatedEvent.getStatus());
        return eventRepository.save(existing);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    // ---- Query Operations ----

    public List<Event> getUpcomingEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc("UPCOMING");
    }

    public List<Event> searchEvents(String department, String eventType, String status, String keyword) {
        return searchEvents(department, eventType, status, keyword, null, null);
    }

    public List<Event> searchEvents(String department, String eventType, String status,
                                    String keyword, LocalDate dateFrom, LocalDate dateTo) {
        String dept = (department == null || department.isBlank()) ? null : department;
        String type = (eventType == null || eventType.isBlank()) ? null : eventType;
        String stat = (status == null || status.isBlank()) ? null : status;
        String kw   = (keyword == null || keyword.isBlank()) ? null : keyword;
        return eventRepository.searchEvents(dept, type, stat, kw, dateFrom, dateTo);
    }

    // ---- Statistics ----

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", eventRepository.count());
        Long totalReg = eventRepository.totalRegistrations();
        stats.put("totalRegistrations", totalReg != null ? totalReg : 0L);
        stats.put("upcomingCount", eventRepository.findByStatusOrderByEventDateAsc("UPCOMING").size());
        stats.put("countByType", eventRepository.countByEventType());
        stats.put("countByDepartment", eventRepository.countByDepartment());
        stats.put("countByStatus", eventRepository.countByStatus());
        stats.put("topEvents", eventRepository.findTopEventsByRegistration().stream().limit(5).toList());
        return stats;
    }
}
