package com.campus.ems;

import com.campus.ems.model.AppUser;
import com.campus.ems.model.Event;
import com.campus.ems.model.Registration;
import com.campus.ems.model.Student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Static factory helpers used across all test classes to avoid repetition.
 */
public class TestDataFactory {

    public static Event makeEvent(String title, String status, int capacity) {
        Event e = new Event();
        e.setTitle(title);
        e.setDescription("A test event description that is long enough to pass validation.");
        e.setEventDate(LocalDate.now().plusDays(7));
        e.setStartTime(LocalTime.of(10, 0));
        e.setEndTime(LocalTime.of(12, 0));
        e.setVenue("Test Venue");
        e.setDepartment("CSE");
        e.setEventType("Workshop");
        e.setTotalCapacity(capacity);
        e.setRegisteredCount(0);
        e.setOrganizer("Test Organizer");
        e.setStatus(status);
        return e;
    }

    public static Student makeStudent(String name, String email) {
        Student s = new Student();
        s.setName(name);
        s.setEmail(email);
        s.setDepartment("CSE");
        s.setYear("2nd Year");
        s.setPhone("9876543210");
        return s;
    }

    public static AppUser makeUser(String username, String role) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword("$2a$10$hashedpassword"); // BCrypt placeholder
        u.setRole(role);
        u.setFullName("Test User");
        u.setEmail(username + "@campus.edu");
        return u;
    }

    public static Registration makeRegistration(Student student, Event event) {
        Registration r = new Registration();
        r.setStudent(student);
        r.setEvent(event);
        r.setStatus("CONFIRMED");
        r.setRegisteredAt(LocalDateTime.now());
        return r;
    }
}
