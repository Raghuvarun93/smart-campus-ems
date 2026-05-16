package com.campus.ems.service;

import com.campus.ems.exception.BusinessException;
import com.campus.ems.model.AppUser;
import com.campus.ems.model.Student;
import com.campus.ems.repository.StudentRepository;
import com.campus.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new student user account and creates a matching Student profile.
     */
    public AppUser registerNewUser(String fullName, String username, String password,
                                   String email, String department, String year, String phone) {

        if (password == null || password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters long.");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new BusinessException("Username '" + username + "' is already taken. Please choose another.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("An account with email '" + email + "' already exists.");
        }

        if (studentRepository.existsByEmail(email)) {
            throw new BusinessException("A student profile with email '" + email + "' already exists.");
        }

        // Create login account
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ROLE_STUDENT");
        user.setFullName(fullName);
        user.setEmail(email);
        userRepository.save(user);

        // Create student profile linked by email
        Student student = new Student();
        student.setName(fullName);
        student.setEmail(email);
        student.setDepartment(department != null && !department.isBlank() ? department : "General");
        student.setYear(year);
        student.setPhone(phone);
        studentRepository.save(student);

        return user;
    }
}
