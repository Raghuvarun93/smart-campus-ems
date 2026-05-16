package com.campus.ems.controller;

import com.campus.ems.model.AppUser;
import com.campus.ems.model.Student;
import com.campus.ems.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // ---- Sign Up ----

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("appUser", new AppUser());
        model.addAttribute("student", new Student());
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(
            @RequestParam String fullName,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String email,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String phone,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("appUser", new AppUser());
            return "signup";
        }

        if (password.length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters.");
            model.addAttribute("appUser", new AppUser());
            return "signup";
        }

        try {
            userService.registerNewUser(fullName, username, password, email, department, year, phone);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Account created! You can now log in with your credentials.");
            return "redirect:/login";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("appUser", new AppUser());
            return "signup";
        }
    }
}
