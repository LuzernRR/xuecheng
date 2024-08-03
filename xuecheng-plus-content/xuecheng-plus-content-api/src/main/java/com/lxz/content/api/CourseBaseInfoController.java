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
import org.springframework.beans.factory.annotation.Autowired;
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
    // swagger注解，生成swagger文档
    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    // pageParams是分页参数，queryCourseParamsDto是查询条件
    // json数据转为对象需要添加@RequestBody
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParams) {
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
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
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程接口")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated() EditCourseDto editCourseDto){
        Long companyId = 1232141425L;
        return courseBaseInfoService.updateCourseBase(companyId, editCourseDto);

    }
}
