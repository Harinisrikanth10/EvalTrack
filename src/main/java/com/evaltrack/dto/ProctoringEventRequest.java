package com.evaltrack.dto;

import com.evaltrack.model.EventType;
import com.evaltrack.model.Severity;
import jakarta.validation.constraints.NotNull;

public class ProctoringEventRequest {

    @NotNull(message = "Event type is required")
    private EventType eventType;

    private String snapshotUrl;
    private String snapshotBase64;
    private Severity severity;

    public ProctoringEventRequest() {}

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getSnapshotUrl() {
        return snapshotUrl;
    }

    public void setSnapshotUrl(String snapshotUrl) {
        this.snapshotUrl = snapshotUrl;
    }

    public String getSnapshotBase64() {
        return snapshotBase64;
    }

    public void setSnapshotBase64(String snapshotBase64) {
        this.snapshotBase64 = snapshotBase64;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
}
