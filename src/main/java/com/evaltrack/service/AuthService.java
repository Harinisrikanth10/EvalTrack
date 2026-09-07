package com.evaltrack.service;

import com.evaltrack.dto.CreateTeacherRequest;
import com.evaltrack.dto.LoginRequest;
import com.evaltrack.dto.LoginResponse;
import com.evaltrack.dto.RegisterRequest;
import com.evaltrack.model.*;
import com.evaltrack.repository.*;
import com.evaltrack.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            TeacherRepository teacherRepository,
            SubjectRepository subjectRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public LoginResponse registerStudent(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (studentRepository.existsByRollNumber(request.getRollNumber())) {
            throw new IllegalArgumentException("Roll number is already registered");
        }

        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), Role.STUDENT);
        user = userRepository.save(user);

        Student student = new Student(user, request.getRollNumber(), request.getName(), request.getAge(), request.getDepartment(), request.getPhotoUrl());
        studentRepository.save(student);

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("name", student.getName());
        profile.put("rollNumber", student.getRollNumber());
        profile.put("department", student.getDepartment());
        profile.put("role", Role.STUDENT);

        return new LoginResponse(token, Role.STUDENT, profile);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());

        if (user.getRole() == Role.STUDENT) {
            Student student = studentRepository.findById(user.getId()).orElse(null);
            if (student != null) {
                profile.put("name", student.getName());
                profile.put("rollNumber", student.getRollNumber());
                profile.put("department", student.getDepartment());
            }
        } else if (user.getRole() == Role.TEACHER) {
            Teacher teacher = teacherRepository.findById(user.getId()).orElse(null);
            if (teacher != null) {
                profile.put("name", teacher.getName());
                profile.put("department", teacher.getDepartment());
            }
        } else if (user.getRole() == Role.ADMIN) {
            profile.put("name", "College Administrator");
        }

        return new LoginResponse(token, user.getRole(), profile);
    }

    @Transactional
    public Map<String, Object> createTeacher(CreateTeacherRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), Role.TEACHER);
        user = userRepository.save(user);

        Teacher teacher = new Teacher(user, request.getName(), request.getDepartment());
        teacherRepository.save(teacher);

        if (request.getSubjectIds() != null && !request.getSubjectIds().isEmpty()) {
            for (UUID subjectId : request.getSubjectIds()) {
                Subject subject = subjectRepository.findById(subjectId).orElse(null);
                if (subject != null) {
                    TeacherSubject ts = new TeacherSubject(teacher, subject);
                    teacherSubjectRepository.save(ts);
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", teacher.getId());
        response.put("email", user.getEmail());
        response.put("name", teacher.getName());
        response.put("department", teacher.getDepartment());
        return response;
    }
}
