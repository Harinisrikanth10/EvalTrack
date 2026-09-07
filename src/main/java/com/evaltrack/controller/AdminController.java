package com.evaltrack.controller;

import com.evaltrack.dto.*;
import com.evaltrack.model.Teacher;
import com.evaltrack.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentProfileDto>> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("id") UUID id) {
        adminService.deleteStudent(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        return ResponseEntity.ok(adminService.getAllTeachers());
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable("id") UUID id) {
        adminService.deleteTeacher(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDto>> getAllSubjects() {
        return ResponseEntity.ok(adminService.getAllSubjects());
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectDto> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.ok(adminService.createSubject(request));
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable("id") UUID id) {
        adminService.deleteSubject(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enrollments")
    public ResponseEntity<Void> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        adminService.enrollStudent(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/enrollments")
    public ResponseEntity<Void> unenrollStudent(@RequestParam("studentId") UUID studentId, @RequestParam("subjectId") UUID subjectId) {
        adminService.unenrollStudent(studentId, subjectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/teacher-subjects")
    public ResponseEntity<Void> assignTeacherSubject(@Valid @RequestBody TeacherSubjectRequest request) {
        adminService.assignTeacherSubject(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<ExamResultDto>> getFlaggedAttempts(@RequestParam(name = "flagged", required = false, defaultValue = "true") boolean flagged) {
        return ResponseEntity.ok(adminService.getFlaggedAttempts());
    }
}
