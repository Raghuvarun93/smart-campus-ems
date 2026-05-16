package com.campus.ems.controller;

import com.campus.ems.model.AppUser;
import com.campus.ems.repository.UserRepository;
import com.campus.ems.service.EventService;
import com.campus.ems.service.RegistrationService;
import com.campus.ems.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @Autowired
    private EventService eventService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("upcomingEvents", eventService.getUpcomingEvents().stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/events")
    public String publicEvents(@RequestParam(required = false) String department,
                               @RequestParam(required = false) String eventType,
                               @RequestParam(required = false) String keyword,
                               Model model) {
        model.addAttribute("events", eventService.searchEvents(department, eventType, "UPCOMING", keyword));
        model.addAttribute("department", department);
        model.addAttribute("eventType", eventType);
        model.addAttribute("keyword", keyword);
        return "student/events";
    }

    @GetMapping("/events/{id}")
    public String publicEventDetail(@PathVariable Long id,
                                    @RequestParam(required = false) Long studentId,
                                    Authentication authentication,
                                    Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        model.addAttribute("students", studentService.getAllStudents());

        // Auto-select logged-in user's student profile
        if (studentId == null && authentication != null) {
            try {
                AppUser user = userRepository.findByUsername(authentication.getName()).orElse(null);
                if (user != null && user.getEmail() != null) {
                    var student = studentService.getStudentByEmail(user.getEmail());
                    studentId = student.getId();
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

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("loginError", "Invalid username or password.");
        if (logout != null) model.addAttribute("logoutMessage", "You have been logged out.");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null && authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/events";
    }
}
