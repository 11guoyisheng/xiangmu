package com.example.basedemo.service;

import com.example.basedemo.model.EnrollRecord;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic processor for the first assignment: deduplicate, sort, and print records.
 */
public class BasicEnrollRecordProcessor {

    public List<EnrollRecord> process(List<EnrollRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        Map<String, EnrollRecord> uniqueRecords = records.stream()
                .filter(record -> record != null
                        && record.getStudentId() != null
                        && record.getCourseId() != null
                        && record.getCourseName() != null)
                .collect(Collectors.toMap(
                        record -> record.getStudentId() + "#" + record.getCourseId(),
                        Function.identity(),
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ));

        List<EnrollRecord> result = uniqueRecords.values().stream()
                .sorted(Comparator.comparing(EnrollRecord::getStudentId)
                        .thenComparing(EnrollRecord::getCourseId))
                .toList();

        result.forEach(record -> System.out.println(record.toString()));
        return result;
    }
}
