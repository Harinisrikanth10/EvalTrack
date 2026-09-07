package com.evaltrack.repository;

import com.evaltrack.model.ProctoringEvent;
import com.evaltrack.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProctoringEventRepository extends JpaRepository<ProctoringEvent, UUID> {
    List<ProctoringEvent> findByAttemptIdOrderByOccurredAtAsc(UUID attemptId);
    long countByAttemptIdAndSeverityIn(UUID attemptId, List<Severity> severities);
}
