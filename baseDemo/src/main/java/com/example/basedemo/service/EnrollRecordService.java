package com.example.basedemo.service;

import com.example.basedemo.dto.EnrollResponse;
import com.example.basedemo.model.CourseInfo;
import com.example.basedemo.model.EnrollRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrollRecordService {

    private static final String PUBLIC_COURSE = "公共课";
    private static final String MAJOR_COURSE = "专业课";
    private static final String ELECTIVE_COURSE = "选修课";
    private static final String UNCATEGORIZED = "未分类";
    private static final String APPROVED = "已通过";
    private static final String REJECTED = "已拒绝";

    private final List<EnrollRecord> currentRecords = new ArrayList<>();
    private final List<String> courseTypes = new ArrayList<>(List.of(PUBLIC_COURSE, MAJOR_COURSE, ELECTIVE_COURSE));
    private final List<CourseInfo> courses = new ArrayList<>();

    public EnrollRecordService() {
        currentRecords.addAll(processRecords(List.of(
                new EnrollRecord("S000001", "C000001", "Java程序设计", MAJOR_COURSE),
                new EnrollRecord("S000002", "C000003", "计算机网络", PUBLIC_COURSE),
                new EnrollRecord("S000003", "C000002", "大学英语", PUBLIC_COURSE),
                new EnrollRecord("S000001", "C000004", "人工智能导论", ELECTIVE_COURSE),
                new EnrollRecord("S000004", "C000005", "数据库系统", MAJOR_COURSE)
        )));
        syncCoursesFromRecords(currentRecords);
    }

    public synchronized EnrollResponse listAll() {
        return buildResponse(new ArrayList<>(currentRecords), "当前共有 " + currentRecords.size() + " 条选课记录");
    }

    public synchronized EnrollResponse importCsv(String csvText) {
        List<EnrollRecord> importedRecords = parseCsv(csvText);
        List<EnrollRecord> processedRecords = processRecords(importedRecords);

        currentRecords.clear();
        currentRecords.addAll(processedRecords);
        syncCoursesFromRecords(processedRecords);

        String message = "导入成功，处理后共有 " + processedRecords.size() + " 条选课记录";
        return buildResponse(new ArrayList<>(currentRecords), message);
    }

    public synchronized EnrollResponse search(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return listAll();
        }

        String normalizedKeyword = keyword.trim().toLowerCase(Locale.ROOT);
        List<EnrollRecord> matchedRecords = currentRecords.stream()
                .filter(record -> containsIgnoreCase(record.getStudentId(), normalizedKeyword)
                        || containsIgnoreCase(record.getCourseId(), normalizedKeyword)
                        || containsIgnoreCase(record.getCourseName(), normalizedKeyword)
                        || containsIgnoreCase(record.getCourseType(), normalizedKeyword))
                .toList();

        String message = matchedRecords.isEmpty() ? "无匹配选课记录" : "检索到 " + matchedRecords.size() + " 条选课记录";
        return buildResponse(matchedRecords, message);
    }

    public synchronized EnrollResponse approve(List<String> recordKeys) {
        return updateAuditStatus(recordKeys, APPROVED);
    }

    public synchronized EnrollResponse reject(List<String> recordKeys) {
        return updateAuditStatus(recordKeys, REJECTED);
    }

    public synchronized EnrollResponse addCourseType(String courseType) {
        if (!StringUtils.hasText(courseType)) {
            return buildResponse(new ArrayList<>(currentRecords), "课程分类名称不能为空");
        }

        String normalizedCourseType = normalizeCourseTypeForManagement(courseType);
        if (!courseTypes.contains(normalizedCourseType)) {
            courseTypes.add(normalizedCourseType);
            return buildResponse(new ArrayList<>(currentRecords), "已添加课程分类：" + normalizedCourseType);
        }
        return buildResponse(new ArrayList<>(currentRecords), "课程分类已存在：" + normalizedCourseType);
    }

    public synchronized EnrollResponse deleteCourseType(String courseType) {
        if (!StringUtils.hasText(courseType)) {
            return buildResponse(new ArrayList<>(currentRecords), "课程分类名称不能为空");
        }

        String normalizedCourseType = normalizeCourseTypeForManagement(courseType);
        if (!courseTypes.remove(normalizedCourseType)) {
            return buildResponse(new ArrayList<>(currentRecords), "课程分类不存在：" + normalizedCourseType);
        }

        currentRecords.stream()
                .filter(record -> normalizedCourseType.equals(record.getCourseType()))
                .forEach(record -> record.setCourseType(UNCATEGORIZED));
        courses.stream()
                .filter(course -> normalizedCourseType.equals(course.getCourseType()))
                .forEach(course -> course.setCourseType(UNCATEGORIZED));

        if (!courseTypes.contains(UNCATEGORIZED)) {
            courseTypes.add(UNCATEGORIZED);
        }
        return buildResponse(new ArrayList<>(currentRecords), "已删除课程分类：" + normalizedCourseType);
    }

    public synchronized EnrollResponse addCourse(String courseId, String courseName, String courseType) {
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(courseName) || !StringUtils.hasText(courseType)) {
            return buildResponse(new ArrayList<>(currentRecords), "课程ID、课程名称和课程分类不能为空");
        }

        CourseInfo course = new CourseInfo(
                courseId.trim(),
                courseName.trim(),
                normalizeCourseTypeForManagement(courseType)
        );
        ensureCourseType(course.getCourseType());

        boolean exists = courses.stream().anyMatch(item -> item.courseKey().equals(course.courseKey()));
        if (!exists) {
            courses.add(course);
            return buildResponse(new ArrayList<>(currentRecords), "已添加课程：" + course.getCourseName());
        }
        return buildResponse(new ArrayList<>(currentRecords), "该分类下课程已存在：" + course.getCourseId());
    }

    List<EnrollRecord> processRecords(List<EnrollRecord> records) {
        Map<String, EnrollRecord> deduplicatedRecords = records.stream()
                .filter(this::hasRequiredFields)
                .map(this::normalizeRecord)
                .collect(Collectors.toMap(
                        EnrollRecord::dedupKey,
                        Function.identity(),
                        (first, duplicate) -> first,
                        LinkedHashMap::new
                ));

        List<EnrollRecord> sortedRecords = deduplicatedRecords.values().stream()
                .sorted(Comparator.comparing(EnrollRecord::getStudentId)
                        .thenComparing(EnrollRecord::getCourseId))
                .toList();

        sortedRecords.forEach(record -> System.out.println(record.toString()));
        return sortedRecords;
    }

    private List<EnrollRecord> parseCsv(String csvText) {
        if (!StringUtils.hasText(csvText)) {
            return List.of();
        }

        List<EnrollRecord> records = new ArrayList<>();
        String[] lines = csvText.split("\\R");
        for (String line : lines) {
            if (!StringUtils.hasText(line)) {
                continue;
            }

            String[] columns = line.split(",", -1);
            if (columns.length < 3) {
                continue;
            }

            String courseType = columns.length >= 4 ? columns[3].trim() : null;
            records.add(new EnrollRecord(
                    columns[0].trim(),
                    columns[1].trim(),
                    columns[2].trim(),
                    courseType
            ));
        }
        return records;
    }

    private EnrollResponse buildResponse(List<EnrollRecord> records, String message) {
        Map<String, List<EnrollRecord>> recordsByType = courseTypes.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> new ArrayList<EnrollRecord>(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        records.stream()
                .collect(Collectors.groupingBy(
                        EnrollRecord::getCourseType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .forEach(recordsByType::put);
        Map<String, List<CourseInfo>> coursesByType = courseTypes.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        type -> new ArrayList<CourseInfo>(),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
        courses.stream()
                .collect(Collectors.groupingBy(
                        CourseInfo::getCourseType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .forEach(coursesByType::put);
        return new EnrollResponse(records, recordsByType, coursesByType, new ArrayList<>(courseTypes), message);
    }

    private EnrollResponse updateAuditStatus(List<String> recordKeys, String auditStatus) {
        if (recordKeys == null || recordKeys.isEmpty()) {
            return buildResponse(new ArrayList<>(currentRecords), "请先选择需要操作的选课记录");
        }

        Set<String> recordKeySet = Set.copyOf(recordKeys);
        int updatedCount = 0;
        for (EnrollRecord record : currentRecords) {
            if (recordKeySet.contains(record.dedupKey())) {
                record.setAuditStatus(auditStatus);
                updatedCount++;
            }
        }

        String message = "已" + ("已通过".equals(auditStatus) ? "通过" : "拒绝") + " " + updatedCount + " 条选课记录";
        return buildResponse(new ArrayList<>(currentRecords), message);
    }

    private boolean hasRequiredFields(EnrollRecord record) {
        return record != null
                && StringUtils.hasText(record.getStudentId())
                && StringUtils.hasText(record.getCourseId())
                && StringUtils.hasText(record.getCourseName());
    }

    private EnrollRecord normalizeRecord(EnrollRecord record) {
        EnrollRecord normalizedRecord = new EnrollRecord(
                record.getStudentId().trim(),
                record.getCourseId().trim(),
                record.getCourseName().trim(),
                normalizeCourseType(record.getCourseType(), record.getCourseName())
        );
        normalizedRecord.setAuditStatus(StringUtils.hasText(record.getAuditStatus()) ? record.getAuditStatus().trim() : "待审核");
        return normalizedRecord;
    }

    private void syncCoursesFromRecords(List<EnrollRecord> records) {
        for (EnrollRecord record : records) {
            CourseInfo course = new CourseInfo(record.getCourseId(), record.getCourseName(), record.getCourseType());
            ensureCourseType(course.getCourseType());
            boolean exists = courses.stream().anyMatch(item -> item.courseKey().equals(course.courseKey()));
            if (!exists) {
                courses.add(course);
            }
        }
    }

    private void ensureCourseType(String courseType) {
        if (!courseTypes.contains(courseType)) {
            courseTypes.add(courseType);
        }
    }

    private String normalizeCourseType(String courseType, String courseName) {
        if (StringUtils.hasText(courseType)) {
            String trimmedType = courseType.trim();
            if (trimmedType.contains("公共")) {
                return PUBLIC_COURSE;
            }
            if (trimmedType.contains("专业")) {
                return MAJOR_COURSE;
            }
            if (trimmedType.contains("选修")) {
                return ELECTIVE_COURSE;
            }
        }

        if (courseName.contains("英语") || courseName.contains("体育") || courseName.contains("思政")
                || courseName.contains("网络")) {
            return PUBLIC_COURSE;
        }
        if (courseName.contains("导论") || courseName.contains("艺术") || courseName.contains("创新")) {
            return ELECTIVE_COURSE;
        }
        return MAJOR_COURSE;
    }

    private String normalizeCourseTypeForManagement(String courseType) {
        String trimmedType = courseType.trim();
        if (trimmedType.contains("公共")) {
            return PUBLIC_COURSE;
        }
        if (trimmedType.contains("专业")) {
            return MAJOR_COURSE;
        }
        if (trimmedType.contains("选修")) {
            return ELECTIVE_COURSE;
        }
        return trimmedType;
    }

    private boolean containsIgnoreCase(String value, String normalizedKeyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedKeyword);
    }
}
