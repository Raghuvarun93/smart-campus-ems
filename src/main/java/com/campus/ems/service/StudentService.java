package com.campus.ems.service;

import com.campus.ems.exception.BusinessException;
import com.campus.ems.exception.ResourceNotFoundException;
import com.campus.ems.model.Student;
import com.campus.ems.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Student registerStudent(Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new BusinessException("A student with email " + student.getEmail() + " already exists.");
        }
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    public Student getStudentByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> searchStudents(String keyword, String department, String year) {
        String kw   = (keyword    == null || keyword.isBlank())    ? null : keyword;
        String dept = (department == null || department.isBlank()) ? null : department;
        String yr   = (year       == null || year.isBlank())       ? null : year;
        return studentRepository.searchStudents(kw, dept, yr);
    }

    public Student updateStudent(Long id, Student updated) {
        Student existing = getStudentById(id);
        existing.setName(updated.getName());
        existing.setDepartment(updated.getDepartment());
        existing.setYear(updated.getYear());
        existing.setPhone(updated.getPhone());
        return studentRepository.save(existing);
    }

    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }
}
