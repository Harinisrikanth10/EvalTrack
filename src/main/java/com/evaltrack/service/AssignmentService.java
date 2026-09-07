package com.evaltrack.service;

import com.evaltrack.dto.SubmissionDetailDto;
import com.evaltrack.dto.SubmitAssignmentRequest;
import com.evaltrack.model.Assignment;
import com.evaltrack.model.AssignmentStatus;
import com.evaltrack.model.AssignmentSubmission;
import com.evaltrack.model.Student;
import com.evaltrack.repository.AssignmentRepository;
import com.evaltrack.repository.AssignmentSubmissionRepository;
import com.evaltrack.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final StudentRepository studentRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository,
            StudentRepository studentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public SubmissionDetailDto submitAssignment(UUID assignmentId, UUID studentId, SubmitAssignmentRequest request) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        LocalDateTime now = LocalDateTime.now();
        AssignmentStatus status = now.isAfter(assignment.getDueAt()) ? AssignmentStatus.LATE : AssignmentStatus.SUBMITTED;

        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElse(new AssignmentSubmission(assignment, student, request.getContentText(), request.getFileUrl(), status));

        submission.setContentText(request.getContentText());
        submission.setFileUrl(request.getFileUrl());
        submission.setSubmittedAt(now);
        submission.setStatus(status);

        submission = assignmentSubmissionRepository.save(submission);

        SubmissionDetailDto dto = new SubmissionDetailDto();
        dto.setSubmissionId(submission.getId());
        dto.setAssignmentId(assignment.getId());
        dto.setAssignmentTitle(assignment.getTitle());
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getName());
        dto.setStudentRollNumber(student.getRollNumber());
        dto.setContentText(submission.getContentText());
        dto.setFileUrl(submission.getFileUrl());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setStatus(submission.getStatus());
        dto.setGrade(submission.getGrade());
        dto.setFeedback(submission.getFeedback());
        dto.setGradedAt(submission.getGradedAt());
        return dto;
    }
}
