package com.campus.ems.smoke;

import com.campus.ems.repository.EventRepository;
import com.campus.ems.repository.StudentRepository;
import com.campus.ems.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SeedDataTest {

    @Autowired UserRepository userRepository;
    @Autowired StudentRepository studentRepository;
    @Autowired EventRepository eventRepository;

    @Test
    void adminUserExists() {
        var admin = userRepository.findByUsername("admin");
        assertThat(admin).isPresent();
        assertThat(admin.get().getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void studentUserExists() {
        var student = userRepository.findByUsername("student");
        assertThat(student).isPresent();
        assertThat(student.get().getRole()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void atLeastThreeSampleStudentsExist() {
        assertThat(studentRepository.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void atLeastFiveUpcomingEventsExist() {
        var upcoming = eventRepository.findByStatusOrderByEventDateAsc("UPCOMING");
        assertThat(upcoming.size()).isGreaterThanOrEqualTo(5);
    }
}
