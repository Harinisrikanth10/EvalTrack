package com.evaltrack.dto;

import java.util.UUID;

public class StudentProfileDto {

    private UUID id;
    private String email;
    private String rollNumber;
    private String name;
    private Integer age;
    private String department;
    private String photoUrl;

    public StudentProfileDto() {}

    public StudentProfileDto(UUID id, String email, String rollNumber, String name, Integer age, String department, String photoUrl) {
        this.id = id;
        this.email = email;
        this.rollNumber = rollNumber;
        this.name = name;
        this.age = age;
        this.department = department;
        this.photoUrl = photoUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
