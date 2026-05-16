package com.campus.ems.controller;

import com.campus.ems.exception.BusinessException;
import com.campus.ems.model.Event;
import com.campus.ems.model.Registration;
import com.campus.ems.model.Student;
import com.campus.ems.service.EventService;
import com.campus.ems.service.RegistrationService;
import com.campus.ems.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private EventService eventService;
    @Autowired private RegistrationService registrationService;
    @Autowired private StudentService studentService;

    // ── Dashboard ──────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = eventService.getStatistics();
        model.addAttribute("stats", stats);
        model.addAttribute("upcomingEvents", eventService.getUpcomingEvents());
        model.addAttribute("allStudents", studentService.getAllStudents());
        return "admin/dashboard";
    }

    // ── List / Search Events ───────────────────────────────────────────────────
    @GetMapping("/events")
    public String listEvents(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        List<Event> events = eventService.searchEvents(department, eventType, status, keyword, dateFrom, dateTo);
        model.addAttribute("events", events);
        model.addAttribute("department", department);
        model.addAttribute("eventType", eventType);
        model.addAttribute("status", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        return "admin/events";
    }

    // ── Create Event ───────────────────────────────────────────────────────────
    @GetMapping("/events/new")
    public String showCreateForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("isEdit", false);
        return "admin/event-form";
    }

    @PostMapping("/events/new")
    public String createEvent(@Valid @ModelAttribute("event") Event event,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "admin/event-form";
        }
        eventService.saveEvent(event);
        redirectAttributes.addFlashAttribute("successMessage",
                "✅ Event \"" + event.getTitle() + "\" created successfully!");
        return "redirect:/admin/events";
    }

    // ── Edit Event ─────────────────────────────────────────────────────────────
    @GetMapping("/events/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.getEventById(id));
        model.addAttribute("isEdit", true);
        return "admin/event-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                              @Valid @ModelAttribute("event") Event event,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "admin/event-form";
        }
        eventService.updateEvent(id, event);
        redirectAttributes.addFlashAttribute("successMessage", "✅ Event updated successfully!");
        return "redirect:/admin/events";
    }

    // ── Delete Event ───────────────────────────────────────────────────────────
    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        eventService.deleteEvent(id);
        redirectAttributes.addFlashAttribute("successMessage", "🗑️ Event deleted successfully.");
        return "redirect:/admin/events";
    }

    // ── View Registrations for an Event ───────────────────────────────────────
    @GetMapping("/events/{id}/registrations")
    public String viewRegistrations(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id);
        List<Registration> registrations = registrationService.getRegistrationsByEvent(id);
        model.addAttribute("event", event);
        model.addAttribute("registrations", registrations);
        return "admin/registrations";
    }

    // ── Admin: Cancel a Registration ───────────────────────────────────────────
    @PostMapping("/registrations/{registrationId}/cancel")
    public String cancelRegistration(@PathVariable Long registrationId,
                                     @RequestParam(required = false) Long eventId,
                                     RedirectAttributes redirectAttributes) {
        try {
            registrationService.cancelRegistration(registrationId);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Registration cancelled.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return eventId != null
                ? "redirect:/admin/events/" + eventId + "/registrations"
                : "redirect:/admin/events";
    }

    // ── Statistics ─────────────────────────────────────────────────────────────
    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("stats", eventService.getStatistics());
        return "admin/statistics";
    }

    // ── Students List / Search ─────────────────────────────────────────────────
    @GetMapping("/students")
    public String manageStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String year,
            Model model) {
        List<Student> students = studentService.searchStudents(keyword, department, year);
        model.addAttribute("students", students);
        model.addAttribute("regCounts", registrationService.getRegistrationCountPerStudent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("department", department);
        model.addAttribute("year", year);
        return "admin/students";
    }

    // ── Student Detail ─────────────────────────────────────────────────────────
    @GetMapping("/students/{id}")
    public String studentDetail(@PathVariable Long id, Model model) {
        Student student = studentService.getStudentById(id);
        List<Registration> registrations = registrationService.getRegistrationsByStudent(id);
        model.addAttribute("student", student);
        model.addAttribute("registrations", registrations);
        return "admin/student-detail";
    }

    // ── Delete Student ─────────────────────────────────────────────────────────
    @PostMapping("/students/{id}/delete")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "🗑️ Student deleted.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete: " + ex.getMessage());
        }
        return "redirect:/admin/students";
    }
}
