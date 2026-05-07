package com.example.basedemo.model;

import java.util.Objects;

public class CourseInfo {

    private String courseId;

    private String courseName;

    private String courseType;

    public CourseInfo() {
    }

    public CourseInfo(String courseId, String courseName, String courseType) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseType = courseType;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String courseKey() {
        return courseType + "#" + courseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CourseInfo that)) {
            return false;
        }
        return Objects.equals(courseId, that.courseId)
                && Objects.equals(courseType, that.courseType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId, courseType);
    }
}
