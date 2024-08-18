package com.lxz.content.api;

import com.lxz.content.model.dto.AddCourseDto;
import com.lxz.content.model.dto.CourseBaseInfoDto;
import com.lxz.content.model.dto.EditCourseDto;
import com.lxz.content.model.dto.QueryCourseParamsDto;
import com.lxz.content.service.CourseBaseInfoService;
import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

// 相当于responsebody+controller
@RestController
// swagger注解，生成swagger文档
@Api(value = "课程基本信息管理", tags = "课程基本信息管理", description = "课程基本信息管理，提供课程的增、删、改、查")
// 用于响应json数据
public class CourseBaseInfoController {
    // 注入service
    @Autowired
    CourseBaseInfoService courseBaseInfoService;


    @ApiOperation("课程分页查询接口")    // swagger注解，生成swagger文档
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")  // 权限校验,拥有这个权限才能访问
    @PostMapping("/course/list")
    // pageParams是分页参数，queryCourseParamsDto是查询条件
    // json数据转为对象需要添加@RequestBody
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        // 获取用户所属机构id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if (StringUtils.isNotEmpty(user.getCompanyId())) {
            companyId = Long.parseLong(user.getCompanyId());
        }
        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParams);
    }

    @ApiOperation("课程新增接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        // 获取用户所属机构id
        Long companyId = 1232141425L;
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }

    @ApiOperation("根据课程id查询接口")
    @GetMapping("/course/{courseId}")
    public CourseBaseInfoDto getCourseBaseInfo(@PathVariable Long courseId){
        // 获取当前用户的身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.out.println("user = " + user.getUsername());
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程接口")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated() EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);

    }
}
