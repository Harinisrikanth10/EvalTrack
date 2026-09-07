package com.evaltrack.security;

import com.evaltrack.model.*;
import com.evaltrack.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("securityService")
public class CustomSecurityService {

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;

    public CustomSecurityService(
            TeacherSubjectRepository teacherSubjectRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository) {
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
    }

    public UserPrincipal getPrincipal(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    public boolean isStudent(Authentication authentication) {
        UserPrincipal principal = getPrincipal(authentication);
        return principal != null && principal.getRole() == Role.STUDENT;
    }

    public boolean isTeacher(Authentication authentication) {
        UserPrincipal principal = getPrincipal(authentication);
        return principal != null && principal.getRole() == Role.TEACHER;
    }

    public boolean isAdmin(Authentication authentication) {
        UserPrincipal principal = getPrincipal(authentication);
        return principal != null && principal.getRole() == Role.ADMIN;
    }

    public boolean canAccessSubject(Authentication authentication, UUID subjectId) {
        UserPrincipal principal = getPrincipal(authentication);
        if (principal == null) return false;
        if (principal.getRole() == Role.ADMIN) return true;
        if (principal.getRole() == Role.TEACHER) {
            return teacherSubjectRepository.existsByTeacherIdAndSubjectId(principal.getId(), subjectId);
        }
        return false;
    }

    public boolean canAccessExam(Authentication authentication, UUID examId) {
        UserPrincipal principal = getPrincipal(authentication);
        if (principal == null) return false;
        if (principal.getRole() == Role.ADMIN) return true;
        Exam exam = examRepository.findById(examId).orElse(null);
        if (exam == null) return false;
        if (principal.getRole() == Role.TEACHER) {
            return teacherSubjectRepository.existsByTeacherIdAndSubjectId(principal.getId(), exam.getSubject().getId());
        }
        return false;
    }

    public boolean canAccessAttempt(Authentication authentication, UUID attemptId) {
        UserPrincipal principal = getPrincipal(authentication);
        if (principal == null) return false;
        if (principal.getRole() == Role.ADMIN) return true;
        ExamAttempt attempt = examAttemptRepository.findById(attemptId).orElse(null);
        if (attempt == null) return false;
        if (principal.getRole() == Role.STUDENT) {
            return attempt.getStudent().getId().equals(principal.getId());
        }
        if (principal.getRole() == Role.TEACHER) {
            return teacherSubjectRepository.existsByTeacherIdAndSubjectId(principal.getId(), attempt.getExam().getSubject().getId());
        }
        return false;
    }

    public boolean canAccessAssignment(Authentication authentication, UUID assignmentId) {
        UserPrincipal principal = getPrincipal(authentication);
        if (principal == null) return false;
        if (principal.getRole() == Role.ADMIN) return true;
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment == null) return false;
        if (principal.getRole() == Role.TEACHER) {
            return teacherSubjectRepository.existsByTeacherIdAndSubjectId(principal.getId(), assignment.getSubject().getId());
        }
        return false;
    }

    public boolean canAccessSubmission(Authentication authentication, UUID submissionId) {
        UserPrincipal principal = getPrincipal(authentication);
        if (principal == null) return false;
        if (principal.getRole() == Role.ADMIN) return true;
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId).orElse(null);
        if (submission == null) return false;
        if (principal.getRole() == Role.STUDENT) {
            return submission.getStudent().getId().equals(principal.getId());
        }
        if (principal.getRole() == Role.TEACHER) {
            return teacherSubjectRepository.existsByTeacherIdAndSubjectId(principal.getId(), submission.getAssignment().getSubject().getId());
        }
        return false;
    }
}
