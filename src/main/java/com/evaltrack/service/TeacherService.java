package com.evaltrack.service;

import com.evaltrack.dto.*;
import com.evaltrack.model.*;
import com.evaltrack.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final SubjectRepository subjectRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ProctoringEventRepository proctoringEventRepository;
    private final TeacherReviewRepository teacherReviewRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final EnrollmentRepository enrollmentRepository;

    public TeacherService(
            TeacherRepository teacherRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            SubjectRepository subjectRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            ProctoringEventRepository proctoringEventRepository,
            TeacherReviewRepository teacherReviewRepository,
            AssignmentRepository assignmentRepository,
            AssignmentSubmissionRepository assignmentSubmissionRepository,
            EnrollmentRepository enrollmentRepository) {
        this.teacherRepository = teacherRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.subjectRepository = subjectRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.proctoringEventRepository = proctoringEventRepository;
        this.teacherReviewRepository = teacherReviewRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> getTeacherSubjects(UUID teacherId) {
        List<TeacherSubject> tsList = teacherSubjectRepository.findByTeacherId(teacherId);
        return tsList.stream().map(ts -> {
            Subject subject = ts.getSubject();
            long enrolledCount = enrollmentRepository.countBySubjectId(subject.getId());
            return new SubjectDto(subject.getId(), subject.getName(), subject.getDepartment(), Collections.singletonList(ts.getTeacher().getName()), enrolledCount);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamDto> getExamsBySubject(UUID subjectId) {
        List<Exam> exams = examRepository.findBySubjectId(subjectId);
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

    @Transactional
    public ExamDto createExam(UUID teacherId, CreateExamRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        Exam exam = new Exam(subject, request.getTitle(), request.getDurationMinutes(), request.getRequiresCamera(), teacher);
        exam = examRepository.save(exam);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            for (CreateQuestionDto qDto : request.getQuestions()) {
                Question question = new Question(exam, qDto.getText(), qDto.getOption1(), qDto.getOption2(), qDto.getOption3(), qDto.getOption4(), qDto.getCorrectOption());
                questionRepository.save(question);
            }
        }

        return new ExamDto(
                exam.getId(),
                subject.getId(),
                subject.getName(),
                exam.getTitle(),
                exam.getDurationMinutes(),
                exam.getRequiresCamera(),
                teacher.getId(),
                teacher.getName(),
                questionRepository.countByExamId(exam.getId())
        );
    }

    @Transactional
    public void deleteExam(UUID examId) {
        questionRepository.deleteByExamId(examId);
        examRepository.deleteById(examId);
    }

    @Transactional(readOnly = true)
    public List<ExamResultDto> getExamAttempts(UUID examId) {
        List<ExamAttempt> attempts = examAttemptRepository.findByExamId(examId);
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
    public AttemptDetailDto getAttemptDetail(UUID attemptId) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found"));

        List<ProctoringEvent> events = proctoringEventRepository.findByAttemptIdOrderByOccurredAtAsc(attemptId);
        List<ProctoringEventDto> eventDtos = events.stream().map(e -> new ProctoringEventDto(
                e.getId(), e.getAttempt().getId(), e.getEventType(), e.getSnapshotUrl(), e.getOccurredAt(), e.getSeverity()
        )).collect(Collectors.toList());

        TeacherReview review = teacherReviewRepository.findByAttemptId(attemptId).orElse(null);

        AttemptDetailDto dto = new AttemptDetailDto();
        dto.setAttemptId(attempt.getId());
        dto.setExamId(attempt.getExam().getId());
        dto.setExamTitle(attempt.getExam().getTitle());
        dto.setSubjectName(attempt.getExam().getSubject().getName());
        dto.setStudentId(attempt.getStudent().getId());
        dto.setStudentName(attempt.getStudent().getName());
        dto.setStudentRollNumber(attempt.getStudent().getRollNumber());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setSubmittedAt(attempt.getSubmittedAt());
        dto.setTotalQuestions(attempt.getTotalQuestions());
        dto.setCorrectAnswers(attempt.getCorrectAnswers());
        dto.setPercentage(attempt.getPercentage());
        dto.setGrade(attempt.getGrade());
        dto.setStatus(attempt.getStatus());
        dto.setProctoringEvents(eventDtos);

        if (review != null) {
            dto.setReviewNote(review.getNote());
            dto.setReviewDecision(review.getDecision());
            dto.setReviewedAt(review.getReviewedAt());
            dto.setReviewerTeacherName(review.getTeacher().getName());
        }

        return dto;
    }

    @Transactional
    public AttemptDetailDto reviewAttempt(UUID teacherId, UUID attemptId, TeacherReviewRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found"));

        TeacherReview review = teacherReviewRepository.findByAttemptId(attemptId)
                .orElse(new TeacherReview(attempt, teacher, request.getNote(), request.getDecision()));

        review.setTeacher(teacher);
        review.setNote(request.getNote());
        review.setDecision(request.getDecision());
        review.setReviewedAt(LocalDateTime.now());
        teacherReviewRepository.save(review);

        if ("CLEARED".equalsIgnoreCase(request.getDecision())) {
            if (attempt.getStatus() == AttemptStatus.FLAGGED) {
                attempt.setStatus(AttemptStatus.SUBMITTED);
                examAttemptRepository.save(attempt);
            }
        }

        return getAttemptDetail(attemptId);
    }

    @Transactional
    public AssignmentDto createAssignment(UUID teacherId, CreateAssignmentRequest request) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        Assignment assignment = new Assignment(
                subject, teacher, request.getTitle(), request.getDescription(),
                request.getWeekNumber(), request.getAttachmentUrl(), request.getDueAt()
        );
        assignment = assignmentRepository.save(assignment);

        long enrolledCount = enrollmentRepository.countBySubjectId(subject.getId());

        AssignmentDto dto = new AssignmentDto();
        dto.setId(assignment.getId());
        dto.setSubjectId(subject.getId());
        dto.setSubjectName(subject.getName());
        dto.setTeacherId(teacher.getId());
        dto.setTeacherName(teacher.getName());
        dto.setTitle(assignment.getTitle());
        dto.setDescription(assignment.getDescription());
        dto.setWeekNumber(assignment.getWeekNumber());
        dto.setAttachmentUrl(assignment.getAttachmentUrl());
        dto.setDueAt(assignment.getDueAt());
        dto.setCreatedAt(assignment.getCreatedAt());
        dto.setEnrolledCount(enrolledCount);
        dto.setSubmittedCount(0);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<AssignmentDto> getAssignmentsBySubject(UUID subjectId) {
        List<Assignment> assignments = assignmentRepository.findBySubjectIdOrderByDueAtAsc(subjectId);
        long enrolledCount = enrollmentRepository.countBySubjectId(subjectId);

        return assignments.stream().map(a -> {
            long submittedCount = assignmentSubmissionRepository.countByAssignmentId(a.getId());
            AssignmentDto dto = new AssignmentDto();
            dto.setId(a.getId());
            dto.setSubjectId(a.getSubject().getId());
            dto.setSubjectName(a.getSubject().getName());
            dto.setTeacherId(a.getTeacher().getId());
            dto.setTeacherName(a.getTeacher().getName());
            dto.setTitle(a.getTitle());
            dto.setDescription(a.getDescription());
            dto.setWeekNumber(a.getWeekNumber());
            dto.setAttachmentUrl(a.getAttachmentUrl());
            dto.setDueAt(a.getDueAt());
            dto.setCreatedAt(a.getCreatedAt());
            dto.setEnrolledCount(enrolledCount);
            dto.setSubmittedCount(submittedCount);
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionDetailDto> getAssignmentSubmissions(UUID assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        List<Enrollment> enrollments = enrollmentRepository.findBySubjectId(assignment.getSubject().getId());
        List<AssignmentSubmission> submissions = assignmentSubmissionRepository.findByAssignmentId(assignmentId);
        Map<UUID, AssignmentSubmission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(s -> s.getStudent().getId(), s -> s));

        List<SubmissionDetailDto> result = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            AssignmentSubmission submission = submissionMap.get(student.getId());

            SubmissionDetailDto dto = new SubmissionDetailDto();
            dto.setAssignmentId(assignment.getId());
            dto.setAssignmentTitle(assignment.getTitle());
            dto.setStudentId(student.getId());
            dto.setStudentName(student.getName());
            dto.setStudentRollNumber(student.getRollNumber());

            if (submission != null) {
                dto.setSubmissionId(submission.getId());
                dto.setContentText(submission.getContentText());
                dto.setFileUrl(submission.getFileUrl());
                dto.setSubmittedAt(submission.getSubmittedAt());
                dto.setStatus(submission.getStatus());
                dto.setGrade(submission.getGrade());
                dto.setFeedback(submission.getFeedback());
                dto.setGradedAt(submission.getGradedAt());
            } else {
                dto.setStatus(AssignmentStatus.PENDING);
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public SubmissionDetailDto gradeSubmission(UUID submissionId, GradeSubmissionRequest request) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        submission.setGrade(request.getGrade());
        submission.setFeedback(request.getFeedback());
        submission.setStatus(AssignmentStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());
        submission = assignmentSubmissionRepository.save(submission);

        SubmissionDetailDto dto = new SubmissionDetailDto();
        dto.setSubmissionId(submission.getId());
        dto.setAssignmentId(submission.getAssignment().getId());
        dto.setAssignmentTitle(submission.getAssignment().getTitle());
        dto.setStudentId(submission.getStudent().getId());
        dto.setStudentName(submission.getStudent().getName());
        dto.setStudentRollNumber(submission.getStudent().getRollNumber());
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
