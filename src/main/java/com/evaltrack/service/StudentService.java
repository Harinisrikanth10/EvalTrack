package com.evaltrack.service;

import com.evaltrack.dto.*;
import com.evaltrack.model.*;
import com.evaltrack.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final QuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            QuestionRepository questionRepository,
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.questionRepository = questionRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public StudentProfileDto getProfile(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        return new StudentProfileDto(
                student.getId(),
                student.getUser().getEmail(),
                student.getRollNumber(),
                student.getName(),
                student.getAge(),
                student.getDepartment(),
                student.getPhotoUrl()
        );
    }

    @Transactional
    public StudentProfileDto updateProfile(UUID studentId, UpdateStudentRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (request.getName() != null) student.setName(request.getName());
        if (request.getAge() != null) student.setAge(request.getAge());
        if (request.getDepartment() != null) student.setDepartment(request.getDepartment());
        if (request.getPhotoUrl() != null) student.setPhotoUrl(request.getPhotoUrl());

        student = studentRepository.save(student);
        return getProfile(student.getId());
    }

    @Transactional
    public void changePassword(UUID studentId, ChangePasswordRequest request) {
        User user = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect old password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<ExamResultDto> getResults(UUID studentId) {
        List<ExamAttempt> attempts = examAttemptRepository.findByStudentId(studentId);
        return attempts.stream().map(attempt -> new ExamResultDto(
                attempt.getId(),
                attempt.getExam().getId(),
                attempt.getExam().getTitle(),
                attempt.getExam().getSubject().getName(),
                attempt.getStudent().getId(),
                attempt.getStudent().getName(),
                attempt.getStudent().getRollNumber(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getPercentage(),
                attempt.getGrade(),
                attempt.getStatus()
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamDto> getExams(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<UUID> subjectIds = enrollments.stream()
                .map(e -> e.getSubject().getId())
                .collect(Collectors.toList());

        if (subjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Exam> exams = examRepository.findBySubjectIdIn(subjectIds);
        return exams.stream().map(exam -> new ExamDto(
                exam.getId(),
                exam.getSubject().getId(),
                exam.getSubject().getName(),
                exam.getTitle(),
                exam.getDurationMinutes(),
                exam.getRequiresCamera(),
                exam.getCreatedBy().getId(),
                exam.getCreatedBy().getName(),
                questionRepository.countByExamId(exam.getId())
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> getAssignments(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<UUID> subjectIds = enrollments.stream()
                .map(e -> e.getSubject().getId())
                .collect(Collectors.toList());

        if (subjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Assignment> assignments = assignmentRepository.findBySubjectIdInOrderByDueAtAsc(subjectIds);
        return assignments.stream().map(a -> mapAssignmentDto(a, studentId)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> getUpcomingAssignments(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<UUID> subjectIds = enrollments.stream()
                .map(e -> e.getSubject().getId())
                .collect(Collectors.toList());

        if (subjectIds.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        List<Assignment> assignments = assignmentRepository.findBySubjectIdInAndDueAtBetweenOrderByDueAtAsc(subjectIds, now, nextWeek);
        return assignments.stream().map(a -> mapAssignmentDto(a, studentId)).collect(Collectors.toList());
    }

    private AssignmentDto mapAssignmentDto(Assignment assignment, UUID studentId) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setSubjectId(assignment.getSubject().getId());
        dto.setSubjectName(assignment.getSubject().getName());
        dto.setTeacherId(assignment.getTeacher().getId());
        dto.setTeacherName(assignment.getTeacher().getName());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setWeekNumber(assignment.getWeekNumber());
        dto.setAttachmentUrl(assignment.getAttachmentUrl());
        dto.setDueAt(assignment.getDueAt());
        dto.setCreatedAt(assignment.getCreatedAt());

        long enrolledCount = enrollmentRepository.countBySubjectId(assignment.getSubject().getId());
        long submittedCount = assignmentSubmissionRepository.countByAssignmentId(assignment.getId());

        dto.setEnrolledCount(enrolledCount);
        dto.setSubmittedCount(submittedCount);

        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), studentId).orElse(null);

        if (submission != null) {
            dto.setStudentSubmissionStatus(submission.getStatus());
            dto.setStudentGrade(submission.getGrade());
            dto.setStudentFeedback(submission.getFeedback());
            dto.setStudentSubmittedAt(submission.getSubmittedAt());
        } else {
            dto.setStudentSubmissionStatus(AssignmentStatus.PENDING);
        }

        return dto;
    }
}
