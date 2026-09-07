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
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final ProctoringEventRepository proctoringEventRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;

    public ExamService(
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamAttemptRepository examAttemptRepository,
            AttemptAnswerRepository attemptAnswerRepository,
            ProctoringEventRepository proctoringEventRepository,
            StudentRepository studentRepository,
            FileStorageService fileStorageService) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.proctoringEventRepository = proctoringEventRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ExamAttempt startAttempt(UUID examId, UUID studentId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Optional<ExamAttempt> existing = examAttemptRepository
                .findByExamIdAndStudentIdAndStatus(examId, studentId, AttemptStatus.IN_PROGRESS);
        if (existing.isPresent()) {
            return existing.get();
        }

        ExamAttempt attempt = new ExamAttempt(exam, student);
        return examAttemptRepository.save(attempt);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getExamQuestions(UUID examId) {
        List<Question> questions = questionRepository.findByExamId(examId);
        return questions.stream().map(q -> new QuestionDto(
                q.getId(),
                q.getExam().getId(),
                q.getText(),
                q.getOption1(),
                q.getOption2(),
                q.getOption3(),
                q.getOption4()
        )).collect(Collectors.toList());
    }

    @Transactional
    public ProctoringEventDto recordProctoringEvent(UUID attemptId, ProctoringEventRequest request) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found"));

        String snapshotUrl = request.getSnapshotUrl();
        if (request.getSnapshotBase64() != null && !request.getSnapshotBase64().isBlank()) {
            snapshotUrl = fileStorageService.storeBase64Image(request.getSnapshotBase64());
        }

        Severity severity = request.getSeverity();
        if (severity == null) {
            if (request.getEventType() == EventType.SNAPSHOT) {
                severity = Severity.INFO;
            } else if (request.getEventType() == EventType.TAB_BLUR || request.getEventType() == EventType.FULLSCREEN_EXIT) {
                severity = Severity.WARNING;
            } else if (request.getEventType() == EventType.CAMERA_LOST || request.getEventType() == EventType.MULTIPLE_FACES) {
                severity = Severity.CRITICAL;
            } else {
                severity = Severity.WARNING;
            }
        }

        ProctoringEvent event = new ProctoringEvent(attempt, request.getEventType(), snapshotUrl, severity);
        event = proctoringEventRepository.save(event);

        long warningOrCriticalCount = proctoringEventRepository
                .countByAttemptIdAndSeverityIn(attemptId, Arrays.asList(Severity.WARNING, Severity.CRITICAL));

        if (warningOrCriticalCount >= 3 && attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            attempt.setStatus(AttemptStatus.FLAGGED);
            examAttemptRepository.save(attempt);
        }

        return new ProctoringEventDto(
                event.getId(),
                event.getAttempt().getId(),
                event.getEventType(),
                event.getSnapshotUrl(),
                event.getOccurredAt(),
                event.getSeverity()
        );
    }

    @Transactional
    public ExamResultDto submitExam(UUID attemptId, SubmitExamRequest request) {
        ExamAttempt attempt = examAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Exam attempt not found"));

        if (attempt.getStatus() == AttemptStatus.SUBMITTED || attempt.getStatus() == AttemptStatus.AUTO_SUBMITTED) {
            return mapToResultDto(attempt);
        }

        List<Question> questions = questionRepository.findByExamId(attempt.getExam().getId());
        int totalQuestions = questions.size();
        int correctAnswers = 0;

        Map<UUID, Integer> chosenAnswers = request.getAnswers() != null ? request.getAnswers() : new HashMap<>();

        for (Question q : questions) {
            Integer chosen = chosenAnswers.getOrDefault(q.getId(), -1);

            AttemptAnswer answer = attemptAnswerRepository
                    .findByAttemptIdAndQuestionId(attemptId, q.getId())
                    .orElse(new AttemptAnswer(attempt, q, chosen));
            answer.setChosenOption(chosen);
            attemptAnswerRepository.save(answer);

            if (chosen != null && chosen.equals(q.getCorrectOption())) {
                correctAnswers++;
            }
        }

        double percentage = totalQuestions > 0 ? ((double) correctAnswers / totalQuestions) * 100.0 : 0.0;
        String grade = calculateGrade(percentage);

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setTotalQuestions(totalQuestions);
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setPercentage(percentage);
        attempt.setGrade(grade);

        if (attempt.getStatus() != AttemptStatus.FLAGGED) {
            attempt.setStatus(AttemptStatus.SUBMITTED);
        }

        attempt = examAttemptRepository.save(attempt);
        return mapToResultDto(attempt);
    }

    public static String calculateGrade(double percentage) {
        if (percentage >= 90.0) return "A+";
        if (percentage >= 75.0) return "A";
        if (percentage >= 60.0) return "B";
        if (percentage >= 40.0) return "C";
        return "Fail";
    }

    private ExamResultDto mapToResultDto(ExamAttempt attempt) {
        return new ExamResultDto(
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
        );
    }
}
