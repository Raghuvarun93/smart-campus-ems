package com.campus.ems.controller;

import com.campus.ems.exception.BusinessException;
import com.campus.ems.model.Event;
import com.campus.ems.model.Registration;
import com.campus.ems.model.Student;
import com.campus.ems.service.EventService;
import com.campus.ems.service.RegistrationService;
import com.campus.ems.service.StudentService;
import jakarta.validation.Valid;
import com.campus.ems.model.AppUser;
import com.campus.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private com.campus.ems.repository.UserRepository userRepository;

    // ---- Student Profile Registration (public) ----

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("student", new Student());
        return "student/register";
    }

    @PostMapping("/register")
    public String registerStudent(@Valid @ModelAttribute("student") Student student,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "student/register";
        }
        try {
            studentService.registerStudent(student);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Profile created! You can now register for events.");
            return "redirect:/events";
        } catch (BusinessException ex) {
            result.rejectValue("email", "email.exists", ex.getMessage());
            return "student/register";
        }
    }

    // ---- Browse Events ----

    @GetMapping("/events")
    public String browseEvents(@RequestParam(required = false) String department,
                               @RequestParam(required = false) String eventType,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        List<Event> events = eventService.searchEvents(department, eventType, "UPCOMING", keyword);
        model.addAttribute("events", events);
        model.addAttribute("department", department);
        model.addAttribute("eventType", eventType);
        model.addAttribute("keyword", keyword);
        return "student/events";
    }

    // ---- Event Detail (authenticated student view) ----

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id,
                              @RequestParam(required = false) Long studentId,
                              Authentication authentication,
                              Model model) {
        Event event = eventService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("students", studentService.getAllStudents());

        // Try to auto-select student by logged-in user's email
        if (studentId == null && authentication != null) {
            try {
                AppUser user = userRepository.findByUsername(authentication.getName()).orElse(null);
                if (user != null && user.getEmail() != null) {
                    Student s = studentService.getStudentByEmail(user.getEmail());
                    studentId = s.getId();
                }
            } catch (Exception ignored) {}
        }

        if (studentId != null) {
            model.addAttribute("alreadyRegistered",
                    registrationService.isStudentRegistered(studentId, id));
            model.addAttribute("selectedStudentId", studentId);
        }
        return "student/event-detail";
    }

    // ---- Register for Event ----

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable Long id,
                                   @RequestParam Long studentId,
                                   RedirectAttributes redirectAttributes) {
        try {
            registrationService.registerStudentForEvent(studentId, id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Successfully registered for the event!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/events/" + id + "?studentId=" + studentId;
    }

    // ---- My Registrations (requires login) ----

    @GetMapping("/my-events")
    public String myEvents(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }
        try {
            // Look up AppUser by username, then find student by email
            AppUser user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                model.addAttribute("noProfile", true);
                return "student/my-events";
            }
            Student student = studentService.getStudentByEmail(user.getEmail());
            List<Registration> registrations = registrationService.getRegistrationsByStudent(student.getId());
            model.addAttribute("registrations", registrations);
            model.addAttribute("student", student);
        } catch (Exception ex) {
            model.addAttribute("noProfile", true);
        }
        return "student/my-events";
    }

    // ---- Submit Feedback ----

    @PostMapping("/feedback/{registrationId}")
    public String submitFeedback(@PathVariable Long registrationId,
                                 @RequestParam int rating,
                                 @RequestParam(required = false) String comment,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            registrationService.submitFeedback(registrationId, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Thank you for your feedback! ⭐");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/my-events";
    }

    // ---- Cancel Registration ----

    @PostMapping("/cancel/{registrationId}")
    public String cancelRegistration(@PathVariable Long registrationId,
                                     RedirectAttributes redirectAttributes) {
        try {
            registrationService.cancelRegistration(registrationId);
            redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled successfully.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/my-events";
    }
}
