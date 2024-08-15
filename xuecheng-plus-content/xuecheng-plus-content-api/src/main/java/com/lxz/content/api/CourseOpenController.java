package com.lxz.content.api;

import com.lxz.content.model.dto.CoursePreviewDto;
import com.lxz.content.service.CourseBaseInfoService;
import com.lxz.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/14 上午10:39
 */
@RestController
@RequestMapping("/open")
public class CourseOpenController {
    // 根据课程id查询课程信息
    @Autowired
    private CoursePublishService coursePublishService;

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    // 根据课程id查询课程信息
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getPreviewInfo(@PathVariable("courseId") Long courseId) {
        // 获取课程预览信息
        return coursePublishService.getCoursePreviewInfo(courseId);
    }
}
