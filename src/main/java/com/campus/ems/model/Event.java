package com.campus.ems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank(message = "Venue is required")
    private String venue;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Event type is required")
    private String eventType;  // Workshop, Seminar, Competition, Cultural, Sports

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 1000, message = "Capacity cannot exceed 1000")
    private int totalCapacity;

    private int registeredCount;

    @NotBlank(message = "Organizer name is required")
    private String organizer;

    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registration> registrations;

    // Helper method
    public int getAvailableSeats() {
        return totalCapacity - registeredCount;
    }

    public boolean isFull() {
        return registeredCount >= totalCapacity;
    }
}
