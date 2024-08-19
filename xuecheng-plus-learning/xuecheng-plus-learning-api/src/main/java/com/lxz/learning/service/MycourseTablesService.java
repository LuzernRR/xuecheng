package com.lxz.learning.service;

import com.lxz.learning.model.dto.XcChooseCourseDto;
import com.lxz.learning.model.dto.XcCourseTablesDto;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/18 下午3:23
 */
// 选课接口
public interface MycourseTablesService {

    // 添加课程
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    // 判断学生是否有学习资格
    public XcCourseTablesDto getLearnStatus(String userId, Long courseId);
}
