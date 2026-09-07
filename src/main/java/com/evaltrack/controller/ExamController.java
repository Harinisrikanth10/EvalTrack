package com.evaltrack.controller;

import com.evaltrack.dto.*;
import com.evaltrack.model.ExamAttempt;
import com.evaltrack.security.UserPrincipal;
import com.evaltrack.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/exams/{examId}/attempts")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<ExamAttempt> startAttempt(
            @PathVariable("examId") UUID examId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(examService.startAttempt(examId, principal.getId()));
    }

    @GetMapping("/exams/{examId}/questions")
    @PreAuthorize("hasRole('STUDENT') or hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<QuestionDto>> getExamQuestions(@PathVariable("examId") UUID examId) {
        return ResponseEntity.ok(examService.getExamQuestions(examId));
    }

    @PostMapping("/attempts/{attemptId}/proctoring-events")
    @PreAuthorize("@securityService.canAccessAttempt(authentication, #attemptId)")
    public ResponseEntity<ProctoringEventDto> recordProctoringEvent(
            @PathVariable("attemptId") UUID attemptId,
            @Valid @RequestBody ProctoringEventRequest request) {
        return ResponseEntity.ok(examService.recordProctoringEvent(attemptId, request));
    }

    @PostMapping("/attempts/{attemptId}/submit")
    @PreAuthorize("@securityService.canAccessAttempt(authentication, #attemptId)")
    public ResponseEntity<ExamResultDto> submitExam(
            @PathVariable("attemptId") UUID attemptId,
            @RequestBody SubmitExamRequest request) {
        return ResponseEntity.ok(examService.submitExam(attemptId, request));
    }
}
