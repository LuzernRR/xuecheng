package com.lxz.learning.api;

import com.lxz.base.exception.XueChengPlusException;
import com.lxz.base.model.PageResult;
import com.lxz.learning.model.dto.MyCourseTableParams;
import com.lxz.learning.model.dto.XcChooseCourseDto;
import com.lxz.learning.model.dto.XcCourseTablesDto;
import com.lxz.learning.model.po.XcCourseTables;
import com.lxz.learning.service.MyCourseTablesService;
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
    MyCourseTablesService myCourseTablesService;

    @ApiOperation("添加选课")
    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseDto addChooseCourse(@PathVariable("courseId") Long courseId) {

        //当前登录的用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("请登录");
        }
        //用户id
        String userId = user.getId();
        //添加选课
        XcChooseCourseDto xcChooseCourseDto = myCourseTablesService.addChooseCourse(userId, courseId);
        return xcChooseCourseDto;
    }

    @ApiOperation("查询学习资格")
    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesDto getLearnstatus(@PathVariable("courseId") Long courseId) {
        //当前登录的用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("请登录");
        }
        //用户id
        String userId = user.getId();

        XcCourseTablesDto learningStatus = myCourseTablesService.getLearnStatus(userId, courseId);

        return learningStatus;

    }


    @ApiOperation("我的课程表")
    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> mycoursetable(MyCourseTableParams params) {
        //当前登录的用户
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null){
            XueChengPlusException.cast("请登录");
        }
        //用户id
        String userId = user.getId();
        params.setUserId(userId);

        PageResult<XcCourseTables> mycoursetables = myCourseTablesService.mycoursetables(params);
        return mycoursetables;
    }
}
