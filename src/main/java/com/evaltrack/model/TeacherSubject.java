package com.evaltrack.model;

import jakarta.persistence.*;

@Entity
@Table(name = "teacher_subjects")
public class TeacherSubject {

    @EmbeddedId
    private TeacherSubjectKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teacherId")
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subjectId")
    @JoinColumn(name = "subject_id")
    private Subject subject;

    public TeacherSubject() {}

    public TeacherSubject(Teacher teacher, Subject subject) {
        this.teacher = teacher;
        this.subject = subject;
        this.id = new TeacherSubjectKey(teacher.getId(), subject.getId());
    }

    public TeacherSubjectKey getId() {
        return id;
    }

    public void setId(TeacherSubjectKey id) {
        this.id = id;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
