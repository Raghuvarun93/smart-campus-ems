package com.campus.ems.service;

import com.campus.ems.exception.BusinessException;
import com.campus.ems.model.AppUser;
import com.campus.ems.repository.StudentRepository;
import com.campus.ems.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerNewUser_throwsBusinessException_whenPasswordShorterThanSixChars() {
        assertThatThrownBy(() ->
                userService.registerNewUser("Test User", "newuser", "abc",
                        "test@campus.edu", "CSE", "2nd Year", "9876543210"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void registerNewUser_throwsBusinessException_whenUsernameAlreadyExists() {
        AppUser existing = new AppUser();
        existing.setUsername("existinguser");
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() ->
                userService.registerNewUser("Test User", "existinguser", "password123",
                        "test@campus.edu", "CSE", "2nd Year", "9876543210"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existinguser");
    }

    @Test
    void registerNewUser_successfulRegistration_setsRoleStudentAndHashedPassword() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("new@campus.edu")).thenReturn(false);
        when(studentRepository.existsByEmail("new@campus.edu")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedvalue");
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.registerNewUser("New User", "newuser", "password123",
                "new@campus.edu", "CSE", "2nd Year", "9876543210");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());

        AppUser saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo("ROLE_STUDENT");
        assertThat(saved.getPassword()).isEqualTo("$2a$10$hashedvalue");
        assertThat(saved.getPassword()).startsWith("$2a$");
    }
}
