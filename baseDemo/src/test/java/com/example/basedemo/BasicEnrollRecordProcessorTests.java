package com.example.basedemo;

import com.example.basedemo.model.EnrollRecord;
import com.example.basedemo.service.BasicEnrollRecordProcessor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BasicEnrollRecordProcessorTests {

    @Test
    void processDeduplicatesByStudentIdAndCourseIdThenSorts() {
        BasicEnrollRecordProcessor processor = new BasicEnrollRecordProcessor();
        List<EnrollRecord> records = List.of(
                new EnrollRecord("S000002", "C000003", "计算机网络"),
                new EnrollRecord("S000001", "C000002", "大学英语"),
                new EnrollRecord("S000001", "C000001", "Java程序设计"),
                new EnrollRecord("S000001", "C000001", "Java程序设计重复记录")
        );

        List<EnrollRecord> result = processor.process(records);

        assertThat(result).extracting(EnrollRecord::toString)
                .containsExactly(
                        "学生ID：S000001，课程ID：C000001，课程名称：Java程序设计",
                        "学生ID：S000001，课程ID：C000002，课程名称：大学英语",
                        "学生ID：S000002，课程ID：C000003，课程名称：计算机网络"
                );
    }
}
