package com.evaltrack.dto;

import com.evaltrack.model.AssignmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssignmentDto {

    private UUID id;
    private UUID subjectId;
    private String subjectName;
    private UUID teacherId;
    private String teacherName;
    private String title;
    private String description;
    private Integer weekNumber;
    private String attachmentUrl;
    private LocalDateTime dueAt;
    private LocalDateTime createdAt;
    private long submittedCount;
    private long enrolledCount;

    // Student specific submission status (populated when queried by student)
    private AssignmentStatus studentSubmissionStatus; // PENDING, SUBMITTED, LATE, GRADED
    private String studentGrade;
    private String studentFeedback;
    private LocalDateTime studentSubmittedAt;

    public AssignmentDto() {}

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

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public long getSubmittedCount() {
        return submittedCount;
    }

    public void setSubmittedCount(long submittedCount) {
        this.submittedCount = submittedCount;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    public AssignmentStatus getStudentSubmissionStatus() {
        return studentSubmissionStatus;
    }

    public void setStudentSubmissionStatus(AssignmentStatus studentSubmissionStatus) {
        this.studentSubmissionStatus = studentSubmissionStatus;
    }

    public String getStudentGrade() {
        return studentGrade;
    }

    public void setStudentGrade(String studentGrade) {
        this.studentGrade = studentGrade;
    }

    public String getStudentFeedback() {
        return studentFeedback;
    }

    public void setStudentFeedback(String studentFeedback) {
        this.studentFeedback = studentFeedback;
    }

    public LocalDateTime getStudentSubmittedAt() {
        return studentSubmittedAt;
    }

    public void setStudentSubmittedAt(LocalDateTime studentSubmittedAt) {
        this.studentSubmittedAt = studentSubmittedAt;
    }
}
