package com.campus.ems.service;

import com.campus.ems.TestDataFactory;
import com.campus.ems.exception.BusinessException;
import com.campus.ems.exception.ResourceNotFoundException;
import com.campus.ems.model.Student;
import com.campus.ems.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void registerStudent_throwsBusinessException_whenEmailAlreadyExists() {
        Student student = TestDataFactory.makeStudent("Alice", "alice@campus.edu");
        when(studentRepository.existsByEmail("alice@campus.edu")).thenReturn(true);

        assertThatThrownBy(() -> studentService.registerStudent(student))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("alice@campus.edu");
    }

    @Test
    void registerStudent_savesStudent_whenEmailIsUnique() {
        Student student = TestDataFactory.makeStudent("Bob", "bob@campus.edu");
        when(studentRepository.existsByEmail("bob@campus.edu")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(student);

        Student result = studentService.registerStudent(student);

        verify(studentRepository).save(student);
        assertThat(result).isEqualTo(student);
    }

    @Test
    void getStudentById_throwsResourceNotFoundException_forNonExistentId() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
