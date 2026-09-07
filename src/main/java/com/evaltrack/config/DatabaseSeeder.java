package com.evaltrack.config;

import com.evaltrack.model.*;
import com.evaltrack.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final AssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            SubjectRepository subjectRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            EnrollmentRepository enrollmentRepository,
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            AssignmentRepository assignmentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.assignmentRepository = assignmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Seeding database with default accounts and demo data...");

            // 1. Create Admin
            User adminUser = userRepository.save(new User("admin@evaltrack.edu", passwordEncoder.encode("Admin@123"), Role.ADMIN));

            // 2. Create Teacher
            User teacherUser = userRepository.save(new User("teacher@evaltrack.edu", passwordEncoder.encode("Teacher@123"), Role.TEACHER));
            Teacher teacher = teacherRepository.save(new Teacher(teacherUser, "Prof. Robert Miller", "Computer Science"));

            // 3. Create Student
            User studentUser = userRepository.save(new User("student@evaltrack.edu", passwordEncoder.encode("Student@123"), Role.STUDENT));
            Student student = studentRepository.save(new Student(studentUser, "CS2026-001", "Alice Johnson", 21, "Computer Science", null));

            // 4. Create Subjects
            Subject subjectOs = new Subject("CS301 - Operating Systems", "Computer Science");
            Subject subjectDs = new Subject("CS201 - Data Structures & Algorithms", "Computer Science");
            subjectOs = subjectRepository.save(subjectOs);
            subjectDs = subjectRepository.save(subjectDs);

            // 5. Link Teacher to Subjects
            teacherSubjectRepository.save(new TeacherSubject(teacher, subjectOs));
            teacherSubjectRepository.save(new TeacherSubject(teacher, subjectDs));

            // 6. Enroll Student in Subjects
            enrollmentRepository.save(new Enrollment(student, subjectOs));
            enrollmentRepository.save(new Enrollment(student, subjectDs));

            // 7. Create Sample Exam
            Exam osExam = new Exam(subjectOs, "OS Midterm Examination 2026", 20, true, teacher);
            osExam = examRepository.save(osExam);

            questionRepository.save(new Question(
                    osExam,
                    "Which CPU scheduling algorithm gives the minimum average waiting time for a given set of processes?",
                    "First-Come, First-Served (FCFS)",
                    "Shortest Job First (SJF)",
                    "Round Robin (RR)",
                    "Priority Scheduling",
                    2
            ));

            questionRepository.save(new Question(
                    osExam,
                    "What is a deadlock condition where each process holds a resource and waits for another resource?",
                    "Mutual Exclusion",
                    "No Preemption",
                    "Hold and Wait",
                    "Circular Wait",
                    3
            ));

            questionRepository.save(new Question(
                    osExam,
                    "Which section of code accesses shared variables and must be executed atomically?",
                    "Critical Section",
                    "Entry Section",
                    "Remainder Section",
                    "Exit Section",
                    1
            ));

            questionRepository.save(new Question(
                    osExam,
                    "Page fault occurs when a requested page is:",
                    "In the cache memory",
                    "Not present in main memory (RAM)",
                    "Corrupted on disk",
                    "Locked by another process",
                    2
            ));

            // 8. Create Sample Weekly Assignment
            Assignment assignment = new Assignment(
                    subjectOs,
                    teacher,
                    "Week 1: Analysis of Process Synchronization & Semaphores",
                    "Write a 2-page report comparing Binary Semaphores and Counting Semaphores with code examples in C/Java.",
                    1,
                    null,
                    LocalDateTime.now().plusDays(5)
            );
            assignmentRepository.save(assignment);

            System.out.println("Database seeding completed successfully!");
            System.out.println("Default Login Credentials:");
            System.out.println(" - Admin: admin@evaltrack.edu / Admin@123");
            System.out.println(" - Teacher: teacher@evaltrack.edu / Teacher@123");
            System.out.println(" - Student: student@evaltrack.edu / Student@123");
        }
    }
}
