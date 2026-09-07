package com.evaltrack.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class EnrollmentRequest {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    public EnrollmentRequest() {}

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }
}
