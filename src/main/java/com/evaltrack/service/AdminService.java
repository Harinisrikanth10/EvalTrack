package com.evaltrack.service;

import com.evaltrack.dto.*;
import com.evaltrack.model.*;
import com.evaltrack.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final AssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            SubjectRepository subjectRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            EnrollmentRepository enrollmentRepository,
            ExamRepository examRepository,
            ExamAttemptRepository examAttemptRepository,
            AssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.examAttemptRepository = examAttemptRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentProfileDto> getAllStudents() {
        return studentRepository.findAll().stream().map(s -> new StudentProfileDto(
                s.getId(), s.getUser().getEmail(), s.getRollNumber(), s.getName(), s.getAge(), s.getDepartment(), s.getPhotoUrl()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void deleteStudent(UUID id) {
        studentRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    @Transactional
    public void deleteTeacher(UUID id) {
        teacherRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> getAllSubjects() {
        return subjectRepository.findAll().stream().map(s -> {
            List<TeacherSubject> tsList = teacherSubjectRepository.findBySubjectId(s.getId());
            List<String> teacherNames = tsList.stream().map(ts -> ts.getTeacher().getName()).collect(Collectors.toList());
            long enrolledCount = enrollmentRepository.countBySubjectId(s.getId());
            return new SubjectDto(s.getId(), s.getName(), s.getDepartment(), teacherNames, enrolledCount);
        }).collect(Collectors.toList());
    }

    @Transactional
    public SubjectDto createSubject(CreateSubjectRequest request) {
        Subject subject = new Subject(request.getName(), request.getDepartment());
        subject = subjectRepository.save(subject);
        return new SubjectDto(subject.getId(), subject.getName(), subject.getDepartment(), List.of(), 0);
    }

    @Transactional
    public void deleteSubject(UUID id) {
        subjectRepository.deleteById(id);
    }

    @Transactional
    public void enrollStudent(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        if (!enrollmentRepository.existsByStudentIdAndSubjectId(student.getId(), subject.getId())) {
            Enrollment enrollment = new Enrollment(student, subject);
            enrollmentRepository.save(enrollment);
        }
    }

    @Transactional
    public void unenrollStudent(UUID studentId, UUID subjectId) {
        enrollmentRepository.deleteByStudentIdAndSubjectId(studentId, subjectId);
    }

    @Transactional
    public void assignTeacherSubject(TeacherSubjectRequest request) {
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        if (!teacherSubjectRepository.existsByTeacherIdAndSubjectId(teacher.getId(), subject.getId())) {
            TeacherSubject ts = new TeacherSubject(teacher, subject);
            teacherSubjectRepository.save(ts);
        }
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        long students = studentRepository.count();
        long teachers = teacherRepository.count();
        long subjects = subjectRepository.count();
        long exams = examRepository.count();
        long attempts = examAttemptRepository.count();
        long flagged = examAttemptRepository.findByStatus(AttemptStatus.FLAGGED).size();
        long assignments = assignmentRepository.count();

        return new AdminStatsDto(students, teachers, subjects, exams, attempts, flagged, assignments);
    }

    @Transactional(readOnly = true)
    public List<ExamResultDto> getFlaggedAttempts() {
        List<ExamAttempt> attempts = examAttemptRepository.findByStatus(AttemptStatus.FLAGGED);
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
}
