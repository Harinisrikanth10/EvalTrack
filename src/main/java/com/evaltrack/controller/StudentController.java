package com.evaltrack.controller;

import com.evaltrack.dto.*;
import com.evaltrack.security.UserPrincipal;
import com.evaltrack.service.AssignmentService;
import com.evaltrack.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
public class StudentController {

    private final StudentService studentService;
    private final AssignmentService assignmentService;

    public StudentController(StudentService studentService, AssignmentService assignmentService) {
        this.studentService = studentService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/students/me")
    public ResponseEntity<StudentProfileDto> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getProfile(principal.getId()));
    }

    @PutMapping("/students/me")
    public ResponseEntity<StudentProfileDto> updateMyProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateStudentRequest request) {
        return ResponseEntity.ok(studentService.updateProfile(principal.getId(), request));
    }

    @PostMapping("/students/me/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        studentService.changePassword(principal.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/students/me/results")
    public ResponseEntity<List<ExamResultDto>> getMyResults(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getResults(principal.getId()));
    }

    @GetMapping("/students/me/exams")
    public ResponseEntity<List<ExamDto>> getMyExams(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getExams(principal.getId()));
    }

    @GetMapping("/students/me/assignments")
    public ResponseEntity<List<AssignmentDto>> getMyAssignments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getAssignments(principal.getId()));
    }

    @GetMapping("/students/me/assignments/upcoming")
    public ResponseEntity<List<AssignmentDto>> getMyUpcomingAssignments(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(studentService.getUpcomingAssignments(principal.getId()));
    }

    @PostMapping("/assignments/{id}/submit")
    public ResponseEntity<SubmissionDetailDto> submitAssignment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") UUID assignmentId,
            @RequestBody SubmitAssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.submitAssignment(assignmentId, principal.getId(), request));
    }
}
