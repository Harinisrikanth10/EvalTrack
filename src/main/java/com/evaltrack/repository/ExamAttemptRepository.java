package com.evaltrack.repository;

import com.evaltrack.model.AttemptStatus;
import com.evaltrack.model.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, UUID> {
    List<ExamAttempt> findByStudentId(UUID studentId);
    List<ExamAttempt> findByExamId(UUID examId);
    List<ExamAttempt> findByExamSubjectId(UUID subjectId);
    List<ExamAttempt> findByStatus(AttemptStatus status);
    Optional<ExamAttempt> findByExamIdAndStudentIdAndStatus(UUID examId, UUID studentId, AttemptStatus status);
}
