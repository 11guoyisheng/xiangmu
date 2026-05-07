package com.example.basedemo.dto;

import com.example.basedemo.model.EnrollRecord;
import com.example.basedemo.model.CourseInfo;

import java.util.List;
import java.util.Map;

public class EnrollResponse {

    private List<EnrollRecord> records;

    private Map<String, List<EnrollRecord>> recordsByType;

    private Map<String, List<CourseInfo>> coursesByType;

    private List<String> courseTypes;

    private String message;

    public EnrollResponse(List<EnrollRecord> records, Map<String, List<EnrollRecord>> recordsByType,
                          Map<String, List<CourseInfo>> coursesByType,
                          List<String> courseTypes, String message) {
        this.records = records;
        this.recordsByType = recordsByType;
        this.coursesByType = coursesByType;
        this.courseTypes = courseTypes;
        this.message = message;
    }

    public List<EnrollRecord> getRecords() {
        return records;
    }

    public void setRecords(List<EnrollRecord> records) {
        this.records = records;
    }

    public Map<String, List<EnrollRecord>> getRecordsByType() {
        return recordsByType;
    }

    public void setRecordsByType(Map<String, List<EnrollRecord>> recordsByType) {
        this.recordsByType = recordsByType;
    }

    public Map<String, List<CourseInfo>> getCoursesByType() {
        return coursesByType;
    }

    public void setCoursesByType(Map<String, List<CourseInfo>> coursesByType) {
        this.coursesByType = coursesByType;
    }

    public List<String> getCourseTypes() {
        return courseTypes;
    }

    public void setCourseTypes(List<String> courseTypes) {
        this.courseTypes = courseTypes;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
