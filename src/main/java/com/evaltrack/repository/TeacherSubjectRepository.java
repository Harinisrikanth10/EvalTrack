package com.evaltrack.repository;

import com.evaltrack.model.TeacherSubject;
import com.evaltrack.model.TeacherSubjectKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, TeacherSubjectKey> {
    List<TeacherSubject> findByTeacherId(UUID teacherId);
    List<TeacherSubject> findBySubjectId(UUID subjectId);
    boolean existsByTeacherIdAndSubjectId(UUID teacherId, UUID subjectId);
    void deleteByTeacherIdAndSubjectId(UUID teacherId, UUID subjectId);
}
