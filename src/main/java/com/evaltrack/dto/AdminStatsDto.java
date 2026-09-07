package com.evaltrack.dto;

public class AdminStatsDto {

    private long totalStudents;
    private long totalTeachers;
    private long totalSubjects;
    private long totalExams;
    private long totalAttempts;
    private long flaggedAttemptsCount;
    private long totalAssignments;

    public AdminStatsDto() {}

    public AdminStatsDto(long totalStudents, long totalTeachers, long totalSubjects, long totalExams, long totalAttempts, long flaggedAttemptsCount, long totalAssignments) {
        this.totalStudents = totalStudents;
        this.totalTeachers = totalTeachers;
        this.totalSubjects = totalSubjects;
        this.totalExams = totalExams;
        this.totalAttempts = totalAttempts;
        this.flaggedAttemptsCount = flaggedAttemptsCount;
        this.totalAssignments = totalAssignments;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public long getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public long getTotalExams() {
        return totalExams;
    }

    public void setTotalExams(long totalExams) {
        this.totalExams = totalExams;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public long getFlaggedAttemptsCount() {
        return flaggedAttemptsCount;
    }

    public void setFlaggedAttemptsCount(long flaggedAttemptsCount) {
        this.flaggedAttemptsCount = flaggedAttemptsCount;
    }

    public long getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(long totalAssignments) {
        this.totalAssignments = totalAssignments;
    }
}
