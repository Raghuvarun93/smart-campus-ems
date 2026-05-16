package com.campus.ems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle Resource Not Found (returns error page for MVC, JSON for REST)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorCode", "404");
        model.addAttribute("errorTitle", "Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // Handle Business Logic Errors
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        model.addAttribute("errorCode", "400");
        model.addAttribute("errorTitle", "Request Failed");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // Handle Validation Errors for @Valid in REST APIs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("message", "Validation failed");
        response.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("errorCode", "500");
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "Something went wrong. Please try again later.");
        return "error";
    }
}
