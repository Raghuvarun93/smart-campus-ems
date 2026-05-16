package com.campus.ems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    private LocalDateTime registeredAt;

    private String status; // CONFIRMED, CANCELLED, WAITLISTED

    // Feedback fields
    @Min(value = 1) @Max(value = 5)
    private Integer feedbackRating;

    @Size(max = 500)
    private String feedbackComment;

    private LocalDateTime feedbackSubmittedAt;

    @PrePersist
    public void prePersist() {
        this.registeredAt = LocalDateTime.now();
        if (this.status == null) this.status = "CONFIRMED";
    }
}
