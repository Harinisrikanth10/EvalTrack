package com.evaltrack.controller;

import com.evaltrack.dto.CreateTeacherRequest;
import com.evaltrack.dto.LoginRequest;
import com.evaltrack.dto.LoginResponse;
import com.evaltrack.dto.RegisterRequest;
import com.evaltrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/admin/create-teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTeacher(@Valid @RequestBody CreateTeacherRequest request) {
        return ResponseEntity.ok(authService.createTeacher(request));
    }
}
