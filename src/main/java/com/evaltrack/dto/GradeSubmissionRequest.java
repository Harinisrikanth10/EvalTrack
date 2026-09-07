package com.evaltrack.dto;

import jakarta.validation.constraints.NotBlank;

public class GradeSubmissionRequest {

    @NotBlank(message = "Grade is required")
    private String grade;

    private String feedback;

    public GradeSubmissionRequest() {}

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
