package com.evaltrack.dto;

import com.evaltrack.model.EventType;
import com.evaltrack.model.Severity;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProctoringEventDto {

    private UUID id;
    private UUID attemptId;
    private EventType eventType;
    private String snapshotUrl;
    private LocalDateTime occurredAt;
    private Severity severity;

    public ProctoringEventDto() {}

    public ProctoringEventDto(UUID id, UUID attemptId, EventType eventType, String snapshotUrl, LocalDateTime occurredAt, Severity severity) {
        this.id = id;
        this.attemptId = attemptId;
        this.eventType = eventType;
        this.snapshotUrl = snapshotUrl;
        this.occurredAt = occurredAt;
        this.severity = severity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(UUID attemptId) {
        this.attemptId = attemptId;
    }

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

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }
}
