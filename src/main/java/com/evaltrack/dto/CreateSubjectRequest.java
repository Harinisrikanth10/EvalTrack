package com.evaltrack.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateSubjectRequest {

    @NotBlank(message = "Subject name is required")
    private String name;

    private String department;

    public CreateSubjectRequest() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
