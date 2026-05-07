package com.example.basedemo.dto;

import java.util.List;

public class AuditRequest {

    private List<String> recordKeys;

    public List<String> getRecordKeys() {
        return recordKeys;
    }

    public void setRecordKeys(List<String> recordKeys) {
        this.recordKeys = recordKeys;
    }
}
