package com.example.basedemo.controller;

import com.example.basedemo.dto.AuditRequest;
import com.example.basedemo.dto.CourseRequest;
import com.example.basedemo.dto.CourseTypeRequest;
import com.example.basedemo.dto.CsvImportRequest;
import com.example.basedemo.dto.EnrollResponse;
import com.example.basedemo.service.EnrollRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 报名记录控制器
 * 提供报名记录的查询、导入、搜索、审批等功能
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollRecordController {

    private final EnrollRecordService enrollRecordService;

    /**
     * 构造函数，注入报名记录服务
     * @param enrollRecordService 报名记录服务实例
     */
    public EnrollRecordController(EnrollRecordService enrollRecordService) {
        this.enrollRecordService = enrollRecordService;
    }

    /**
     * 获取所有报名记录列表
     * @return 包含所有报名记录的响应对象
     */
    @GetMapping
    public EnrollResponse listAll() {
        return enrollRecordService.listAll();
    }

    /**
     * 从CSV文本导入报名记录
     * @param request 包含CSV文本内容的请求对象
     * @return 导入操作的结果响应
     */
    @PostMapping("/import")
    public EnrollResponse importCsv(@RequestBody CsvImportRequest request) {
        return enrollRecordService.importCsv(request.getCsvText());
    }

    /**
     * 根据关键字搜索报名记录
     * @param keyword 搜索关键字（可选，为空时返回所有记录）
     * @return 符合搜索条件的报名记录列表
     */
    @GetMapping("/search")
    public EnrollResponse search(@RequestParam(required = false) String keyword) {
        return enrollRecordService.search(keyword);
    }

    /**
     * 审批通过指定的报名记录
     * @param request 包含待审批记录键的请求对象
     * @return 审批操作的结果响应
     */
    @PostMapping("/approve")
    public EnrollResponse approve(@RequestBody AuditRequest request) {
        return enrollRecordService.approve(request.getRecordKeys());
    }

    /**
     * 拒绝指定的报名记录
     * @param request 包含待拒绝记录键的请求对象
     * @return 拒绝操作的结果响应
     */
    @PostMapping("/reject")
    public EnrollResponse reject(@RequestBody AuditRequest request) {
        return enrollRecordService.reject(request.getRecordKeys());
    }

    @PostMapping("/types")
    public EnrollResponse addCourseType(@RequestBody CourseTypeRequest request) {
        return enrollRecordService.addCourseType(request.getCourseType());
    }

    @PostMapping("/types/delete")
    public EnrollResponse deleteCourseType(@RequestBody CourseTypeRequest request) {
        return enrollRecordService.deleteCourseType(request.getCourseType());
    }

    @PostMapping("/courses")
    public EnrollResponse addCourse(@RequestBody CourseRequest request) {
        return enrollRecordService.addCourse(request.getCourseId(), request.getCourseName(), request.getCourseType());
    }
}
