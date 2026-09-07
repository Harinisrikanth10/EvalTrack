package com.evaltrack.dto;

import java.util.List;
import java.util.UUID;

public class SubjectDto {

    private UUID id;
    private String name;
    private String department;
    private List<String> teacherNames;
    private long enrolledCount;

    public SubjectDto() {}

    public SubjectDto(UUID id, String name, String department, List<String> teacherNames, long enrolledCount) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.teacherNames = teacherNames;
        this.enrolledCount = enrolledCount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public List<String> getTeacherNames() {
        return teacherNames;
    }

    public void setTeacherNames(List<String> teacherNames) {
        this.teacherNames = teacherNames;
    }

    public long getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(long enrolledCount) {
        this.enrolledCount = enrolledCount;
    }
}
