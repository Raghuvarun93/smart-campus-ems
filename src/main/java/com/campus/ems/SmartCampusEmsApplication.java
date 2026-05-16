package com.campus.ems;

import com.campus.ems.model.*;
import com.campus.ems.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartCampusEmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusEmsApplication.class, args);
    }

    // Seed sample data on startup
    @Bean
    public CommandLineRunner seedData(
            EventRepository eventRepo,
            StudentRepository studentRepo,
            UserRepository userRepo,
            PasswordEncoder encoder) {
        return args -> {

            // Create admin user
            if (userRepo.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                admin.setFullName("Campus Administrator");
                admin.setEmail("admin@campus.edu");
                userRepo.save(admin);
            }

            // Create student user
            if (userRepo.findByUsername("student").isEmpty()) {
                AppUser studentUser = new AppUser();
                studentUser.setUsername("student");
                studentUser.setPassword(encoder.encode("student123"));
                studentUser.setRole("ROLE_STUDENT");
                studentUser.setFullName("Demo Student");
                studentUser.setEmail("student@campus.edu");
                userRepo.save(studentUser);
            }

            // Sample students
            if (studentRepo.count() == 0) {
                studentRepo.save(new Student(null, "Varun Kumar", "varun@campus.edu", "CSE", "3rd Year", "9876543210", null));
                studentRepo.save(new Student(null, "Priya Sharma", "priya@campus.edu", "ECE", "2nd Year", "9876543211", null));
                studentRepo.save(new Student(null, "Arjun Mehta", "arjun@campus.edu", "MECH", "4th Year", "9876543212", null));
            }

            // Sample events
            if (eventRepo.count() == 0) {
                eventRepo.save(new Event(null, "AI & Machine Learning Workshop",
                        "Hands-on workshop covering ML fundamentals, neural networks, and real-world applications using Python and TensorFlow.",
                        LocalDate.now().plusDays(5), LocalTime.of(10, 0), LocalTime.of(16, 0),
                        "Seminar Hall A", "CSE", "Workshop", 60, 0, "Dr. Ramesh Kumar", "UPCOMING", null));

                eventRepo.save(new Event(null, "Inter-College Hackathon 2025",
                        "24-hour coding competition open to all departments. Build innovative solutions for real campus problems.",
                        LocalDate.now().plusDays(10), LocalTime.of(9, 0), LocalTime.of(9, 0),
                        "Main Auditorium", "ALL", "Competition", 100, 0, "Prof. Anita Singh", "UPCOMING", null));

                eventRepo.save(new Event(null, "Career Guidance Seminar",
                        "Industry experts from top MNCs will share insights on placement preparation, resume building, and interview skills.",
                        LocalDate.now().plusDays(3), LocalTime.of(14, 0), LocalTime.of(17, 0),
                        "Conference Hall B", "ALL", "Seminar", 150, 0, "Mr. Suresh Nair", "UPCOMING", null));

                eventRepo.save(new Event(null, "Cloud Computing Bootcamp",
                        "3-day intensive bootcamp on AWS, Azure, and GCP with hands-on labs and certification prep.",
                        LocalDate.now().plusDays(15), LocalTime.of(9, 30), LocalTime.of(17, 30),
                        "Lab Block 2", "CSE", "Workshop", 40, 0, "Ms. Kavitha Reddy", "UPCOMING", null));

                eventRepo.save(new Event(null, "Cultural Fest - Kaleidoscope",
                        "Annual cultural extravaganza featuring music, dance, drama, and art competitions across departments.",
                        LocalDate.now().plusDays(20), LocalTime.of(10, 0), LocalTime.of(22, 0),
                        "Open Air Theatre", "ALL", "Cultural", 500, 0, "Student Council", "UPCOMING", null));
            }
        };
    }
}
