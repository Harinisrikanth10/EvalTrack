package com.evaltrack.controller;

import com.evaltrack.dto.*;
import com.evaltrack.security.UserPrincipal;
import com.evaltrack.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectDto>> getTeacherSubjects(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(teacherService.getTeacherSubjects(principal.getId()));
    }

    @GetMapping("/exams")
    @PreAuthorize("@securityService.canAccessSubject(authentication, #subjectId)")
    public ResponseEntity<List<ExamDto>> getExamsBySubject(@RequestParam("subjectId") UUID subjectId) {
        return ResponseEntity.ok(teacherService.getExamsBySubject(subjectId));
    }

    @PostMapping("/exams")
    @PreAuthorize("@securityService.canAccessSubject(authentication, #request.subjectId)")
    public ResponseEntity<ExamDto> createExam(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateExamRequest request) {
        return ResponseEntity.ok(teacherService.createExam(principal.getId(), request));
    }

    @DeleteMapping("/exams/{examId}")
    @PreAuthorize("@securityService.canAccessExam(authentication, #examId)")
    public ResponseEntity<Void> deleteExam(@PathVariable("examId") UUID examId) {
        teacherService.deleteExam(examId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/exams/{examId}/attempts")
    @PreAuthorize("@securityService.canAccessExam(authentication, #examId)")
    public ResponseEntity<List<ExamResultDto>> getExamAttempts(@PathVariable("examId") UUID examId) {
        return ResponseEntity.ok(teacherService.getExamAttempts(examId));
    }

    @GetMapping("/attempts/{attemptId}")
    @PreAuthorize("@securityService.canAccessAttempt(authentication, #attemptId)")
    public ResponseEntity<AttemptDetailDto> getAttemptDetail(@PathVariable("attemptId") UUID attemptId) {
        return ResponseEntity.ok(teacherService.getAttemptDetail(attemptId));
    }

    @PostMapping("/attempts/{attemptId}/review")
    @PreAuthorize("@securityService.canAccessAttempt(authentication, #attemptId)")
    public ResponseEntity<AttemptDetailDto> reviewAttempt(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("attemptId") UUID attemptId,
            @Valid @RequestBody TeacherReviewRequest request) {
        return ResponseEntity.ok(teacherService.reviewAttempt(principal.getId(), attemptId, request));
    }

    @PostMapping("/assignments")
    @PreAuthorize("@securityService.canAccessSubject(authentication, #request.subjectId)")
    public ResponseEntity<AssignmentDto> createAssignment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateAssignmentRequest request) {
        return ResponseEntity.ok(teacherService.createAssignment(principal.getId(), request));
    }

    @GetMapping("/assignments")
    @PreAuthorize("@securityService.canAccessSubject(authentication, #subjectId)")
    public ResponseEntity<List<AssignmentDto>> getAssignmentsBySubject(@RequestParam("subjectId") UUID subjectId) {
        return ResponseEntity.ok(teacherService.getAssignmentsBySubject(subjectId));
    }

    @GetMapping("/assignments/{id}/submissions")
    @PreAuthorize("@securityService.canAccessAssignment(authentication, #assignmentId)")
    public ResponseEntity<List<SubmissionDetailDto>> getAssignmentSubmissions(@PathVariable("id") UUID assignmentId) {
        return ResponseEntity.ok(teacherService.getAssignmentSubmissions(assignmentId));
    }

    @PostMapping("/submissions/{id}/grade")
    @PreAuthorize("@securityService.canAccessSubmission(authentication, #submissionId)")
    public ResponseEntity<SubmissionDetailDto> gradeSubmission(
            @PathVariable("id") UUID submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {
        return ResponseEntity.ok(teacherService.gradeSubmission(submissionId, request));
    }
}
