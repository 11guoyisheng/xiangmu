package com.example.basedemo.dto;

import com.example.basedemo.model.EnrollRecord;

import java.util.List;
import java.util.Map;

public class EnrollResponse {

    private List<EnrollRecord> records;

    private Map<String, List<EnrollRecord>> recordsByType;

    private String message;

    public EnrollResponse(List<EnrollRecord> records, Map<String, List<EnrollRecord>> recordsByType, String message) {
        this.records = records;
        this.recordsByType = recordsByType;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
