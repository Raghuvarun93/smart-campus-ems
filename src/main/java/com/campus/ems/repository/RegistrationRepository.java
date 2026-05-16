package com.campus.ems.repository;

import com.campus.ems.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    List<Registration> findByStudentId(Long studentId);

    List<Registration> findByEventId(Long eventId);

    Optional<Registration> findByStudentIdAndEventId(Long studentId, Long eventId);

    boolean existsByStudentIdAndEventId(Long studentId, Long eventId);

    @Query("SELECT COUNT(r) FROM Registration r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    long countConfirmedByEventId(@Param("eventId") Long eventId);

    // Average feedback rating per event
    @Query("SELECT AVG(r.feedbackRating) FROM Registration r WHERE r.event.id = :eventId AND r.feedbackRating IS NOT NULL")
    Double avgRatingByEventId(@Param("eventId") Long eventId);

    // Events registered by student
    @Query("SELECT r FROM Registration r JOIN FETCH r.event WHERE r.student.id = :studentId ORDER BY r.registeredAt DESC")
    List<Registration> findRegistrationsWithEventsByStudentId(@Param("studentId") Long studentId);

    // Count confirmed registrations per student (for admin students page)
    @Query("SELECT r.student.id, COUNT(r) FROM Registration r WHERE r.status = 'CONFIRMED' GROUP BY r.student.id")
    List<Object[]> countRegistrationsPerStudent();
}
