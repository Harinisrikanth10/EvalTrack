package com.evaltrack.repository;

import com.evaltrack.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExamRepository extends JpaRepository<Exam, UUID> {
    List<Exam> findBySubjectId(UUID subjectId);
    List<Exam> findBySubjectIdIn(List<UUID> subjectIds);
}
