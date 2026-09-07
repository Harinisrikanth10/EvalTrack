package com.evaltrack.repository;

import com.evaltrack.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findBySubjectIdOrderByDueAtAsc(UUID subjectId);
    List<Assignment> findBySubjectIdInOrderByDueAtAsc(List<UUID> subjectIds);
    List<Assignment> findBySubjectIdInAndDueAtBetweenOrderByDueAtAsc(List<UUID> subjectIds, LocalDateTime start, LocalDateTime end);
}
