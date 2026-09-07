package com.evaltrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class CreateExamRequest {

    @NotNull(message = "Subject ID is required")
    private UUID subjectId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Duration in minutes is required")
    private Integer durationMinutes;

    private Boolean requiresCamera = true;

    private List<CreateQuestionDto> questions;

    public CreateExamRequest() {}

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Boolean getRequiresCamera() {
        return requiresCamera;
    }

    public void setRequiresCamera(Boolean requiresCamera) {
        this.requiresCamera = requiresCamera;
    }

    public List<CreateQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<CreateQuestionDto> questions) {
        this.questions = questions;
    }
}
