package com.example.basedemo;

import com.example.basedemo.dto.EnrollResponse;
import com.example.basedemo.model.EnrollRecord;
import com.example.basedemo.service.EnrollRecordService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EnrollRecordServiceTests {

    @Test
    void importCsvDeduplicatesSortsAndClassifiesRecords() {
        EnrollRecordService service = new EnrollRecordService();
        String csvText = """
                S000002,C000003,计算机网络,公共课
                S000001,C000001,Java程序设计,专业课
                S000001,C000001,Java程序设计A,专业课
                S000001,C000004,创新创业基础,
                """;

        EnrollResponse response = service.importCsv(csvText);

        assertThat(response.getRecords()).extracting(EnrollRecord::dedupKey)
                .containsExactly("S000001#C000001", "S000001#C000004", "S000002#C000003");
        assertThat(response.getRecordsByType()).containsKeys("专业课", "选修课", "公共课");
    }

    @Test
    void searchMatchesByStudentCourseNameAndType() {
        EnrollRecordService service = new EnrollRecordService();

        assertThat(service.search("S000001").getRecords()).hasSize(2);
        assertThat(service.search("C000003").getRecords()).hasSize(1);
        assertThat(service.search("数据库").getRecords()).hasSize(1);
        assertThat(service.search("公共课").getRecords()).hasSize(2);
        assertThat(service.search("不存在").getMessage()).isEqualTo("无匹配选课记录");
    }

    @Test
    void approveAndRejectUpdatesSelectedRecordsOnly() {
        EnrollRecordService service = new EnrollRecordService();

        EnrollResponse approvedResponse = service.approve(List.of("S000001#C000001", "S000002#C000003"));

        assertThat(approvedResponse.getRecords())
                .filteredOn(record -> List.of("S000001#C000001", "S000002#C000003").contains(record.dedupKey()))
                .extracting(EnrollRecord::getAuditStatus)
                .containsOnly("已通过");

        EnrollResponse rejectedResponse = service.reject(List.of("S000001#C000001"));

        assertThat(rejectedResponse.getRecords())
                .filteredOn(record -> record.dedupKey().equals("S000001#C000001"))
                .extracting(EnrollRecord::getAuditStatus)
                .containsExactly("已拒绝");
        assertThat(rejectedResponse.getRecords())
                .filteredOn(record -> record.dedupKey().equals("S000002#C000003"))
                .extracting(EnrollRecord::getAuditStatus)
                .containsExactly("已通过");
    }

    @Test
    void addAndDeleteCourseTypeManagesTypeList() {
        EnrollRecordService service = new EnrollRecordService();

        EnrollResponse addedResponse = service.addCourseType("实验课");

        assertThat(addedResponse.getCourseTypes()).contains("实验课");
        assertThat(addedResponse.getRecordsByType()).containsKey("实验课");
        assertThat(addedResponse.getRecordsByType().get("实验课")).isEmpty();

        EnrollResponse deletedResponse = service.deleteCourseType("专业课");

        assertThat(deletedResponse.getCourseTypes()).doesNotContain("专业课");
        assertThat(deletedResponse.getCourseTypes()).contains("未分类");
        assertThat(deletedResponse.getRecords())
                .filteredOn(record -> record.dedupKey().equals("S000001#C000001"))
                .extracting(EnrollRecord::getCourseType)
                .containsExactly("未分类");
    }

    @Test
    void addCourseCreatesCourseUnderSelectedType() {
        EnrollRecordService service = new EnrollRecordService();

        EnrollResponse response = service.addCourse("C000099", "软件测试", "实验课");

        assertThat(response.getCourseTypes()).contains("实验课");
        assertThat(response.getCoursesByType().get("实验课"))
                .extracting(course -> course.getCourseId() + "#" + course.getCourseName())
                .containsExactly("C000099#软件测试");
    }

    @Test
    void largeImportKeepsStablePerformanceForSimpleProcessing() {
        EnrollRecordService service = new EnrollRecordService();
        StringBuilder csvText = new StringBuilder();
        for (int i = 1; i <= 1_200; i++) {
            csvText.append(String.format("S%06d,C%06d,课程%d,专业课%n", i, i, i));
        }

        long startedAt = System.nanoTime();
        EnrollResponse response = service.importCsv(csvText.toString());
        long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;

        assertThat(response.getRecords()).hasSize(1_200);
        assertThat(elapsedMillis).isLessThan(1_000);
    }
}
