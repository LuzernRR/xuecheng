package com.lxz.learning.service;

import com.lxz.base.model.PageResult;
import com.lxz.learning.model.dto.MyCourseTableParams;
import com.lxz.learning.model.dto.XcChooseCourseDto;
import com.lxz.learning.model.dto.XcCourseTablesDto;
import com.lxz.learning.model.po.XcCourseTables;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/18 下午3:23
 */
// 选课接口
public interface MyCourseTablesService {

    // 添加课程
    public XcChooseCourseDto addChooseCourse(String userId, Long courseId);

    // 判断学生是否有学习资格
    public XcCourseTablesDto getLearnStatus(String userId, Long courseId);
    /**
     * 保存选课成功状态
     * @param chooseCourseId
     * @return
     */
    public boolean saveChooseCourseSuccess(String chooseCourseId);

    /**
     * @description 我的课程表
     * @param params
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.learning.model.po.XcCourseTables>
     * @author Mr.M
     * @date 2022/10/27 9:24
     */
    public PageResult<XcCourseTables> mycoursetables(MyCourseTableParams params);

}
