package com.evaltrack.dto;

import java.util.UUID;

public class ExamDto {

    private UUID id;
    private UUID subjectId;
    private String subjectName;
    private String title;
    private Integer durationMinutes;
    private Boolean requiresCamera;
    private UUID createdById;
    private String createdByName;
    private long totalQuestions;

    public ExamDto() {}

    public ExamDto(UUID id, UUID subjectId, String subjectName, String title, Integer durationMinutes, Boolean requiresCamera, UUID createdById, String createdByName, long totalQuestions) {
        this.id = id;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.requiresCamera = requiresCamera;
        this.createdById = createdById;
        this.createdByName = createdByName;
        this.totalQuestions = totalQuestions;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
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

    public UUID getCreatedById() {
        return createdById;
    }

    public void setCreatedById(UUID createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
}
