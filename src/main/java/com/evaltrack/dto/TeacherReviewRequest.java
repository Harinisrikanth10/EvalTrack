package com.evaltrack.dto;

import jakarta.validation.constraints.NotBlank;

public class TeacherReviewRequest {

    private String note;

    @NotBlank(message = "Decision is required")
    private String decision; // CLEARED | FLAGGED_FOR_ACTION

    public TeacherReviewRequest() {}

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }
}
