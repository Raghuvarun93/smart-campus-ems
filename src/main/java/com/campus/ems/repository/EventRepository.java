package com.campus.ems.repository;

import com.campus.ems.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events by status
    List<Event> findByStatusOrderByEventDateAsc(String status);

    // Find events by department
    List<Event> findByDepartmentOrderByEventDateAsc(String department);

    // Find events by type
    List<Event> findByEventTypeOrderByEventDateAsc(String eventType);

    // Find events by date range
    List<Event> findByEventDateBetweenOrderByEventDateAsc(LocalDate start, LocalDate end);

    // Search with filters (JPQL) — department, type, status, keyword, date range
    @Query("SELECT e FROM Event e WHERE " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%',:keyword,'%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND " +
           "(:dateFrom IS NULL OR e.eventDate >= :dateFrom) AND " +
           "(:dateTo IS NULL OR e.eventDate <= :dateTo)" +
           " ORDER BY e.eventDate ASC")
    List<Event> searchEvents(
            @Param("department") String department,
            @Param("eventType") String eventType,
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

    // Count by type (for statistics)
    @Query("SELECT e.eventType, COUNT(e) FROM Event e GROUP BY e.eventType")
    List<Object[]> countByEventType();

    // Count by department
    @Query("SELECT e.department, COUNT(e) FROM Event e GROUP BY e.department")
    List<Object[]> countByDepartment();

    // Count by status
    @Query("SELECT e.status, COUNT(e) FROM Event e GROUP BY e.status")
    List<Object[]> countByStatus();

    // Total registrations across all events
    @Query("SELECT SUM(e.registeredCount) FROM Event e")
    Long totalRegistrations();

    // Events with most registrations
    @Query("SELECT e FROM Event e ORDER BY e.registeredCount DESC")
    List<Event> findTopEventsByRegistration();

    // Upcoming events
    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDate date);
}
