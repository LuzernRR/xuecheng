package com.lxz.learning.api;

import com.lxz.base.exception.XueChengPlusException;
import com.lxz.base.model.PageResult;
import com.lxz.learning.model.dto.MyCourseTableParams;
import com.lxz.learning.model.dto.XcChooseCourseDto;
import com.lxz.learning.model.dto.XcCourseTablesDto;
import com.lxz.learning.model.po.XcCourseTables;
import com.lxz.learning.service.MycourseTablesService;
import com.lxz.learning.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.M
 * @version 1.0
 * @description 我的课程表接口
 * @date 2022/10/25 9:40
 */

@Api(value = "我的课程表接口", tags = "我的课程表接口")
@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    MycourseTablesService mycourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {
        // 1.获取当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("用户未登录");
        }
        String id = user.getId();

        // 2.添加选课
        XcChooseCourseDto xcChooseCourseDto = mycourseTablesService.addChooseCourse(id, courseId);
        return xcChooseCourseDto;
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        // 1.获取当前登录用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if (user == null) {
            XueChengPlusException.cast("用户未登录");
        }
        String id = user.getId();
        XcCourseTablesDto learnStatus = mycourseTablesService.getLearnStatus(id, courseId);
        return learnStatus;

    }

    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        return null;
    }

}
