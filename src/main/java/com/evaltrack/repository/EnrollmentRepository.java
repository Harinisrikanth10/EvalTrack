package com.evaltrack.repository;

import com.evaltrack.model.Enrollment;
import com.evaltrack.model.EnrollmentKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentKey> {
    List<Enrollment> findByStudentId(UUID studentId);
    List<Enrollment> findBySubjectId(UUID subjectId);
    long countBySubjectId(UUID subjectId);
    boolean existsByStudentIdAndSubjectId(UUID studentId, UUID subjectId);
    void deleteByStudentIdAndSubjectId(UUID studentId, UUID subjectId);
}
