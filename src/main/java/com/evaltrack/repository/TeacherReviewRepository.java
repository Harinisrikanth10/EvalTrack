package com.evaltrack.repository;

import com.evaltrack.model.TeacherReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherReviewRepository extends JpaRepository<TeacherReview, UUID> {
    Optional<TeacherReview> findByAttemptId(UUID attemptId);
}
