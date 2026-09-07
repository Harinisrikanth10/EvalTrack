package com.evaltrack.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class TeacherSubjectRequest {

    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    public TeacherSubjectRequest() {}

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }
}
