package com.evaltrack.dto;

import java.util.Map;
import java.util.UUID;

public class SubmitExamRequest {

    // Map of questionId -> chosenOption (1-4, or -1 if skipped)
    private Map<UUID, Integer> answers;

    public SubmitExamRequest() {}

    public Map<UUID, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<UUID, Integer> answers) {
        this.answers = answers;
    }
}
