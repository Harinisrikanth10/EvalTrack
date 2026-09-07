package com.evaltrack.dto;

public class SubmitAssignmentRequest {

    private String contentText;
    private String fileUrl;

    public SubmitAssignmentRequest() {}

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
