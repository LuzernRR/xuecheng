package com.lxz.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 相当于responsebody+controller
@RestController
// swagger注解，生成swagger文档
@Api(value = "课程基本信息管理", tags = "课程基本信息管理", description = "课程基本信息管理，提供课程的增、删、改、查")
// 用于响应json数据
public class CourseBaseInfoController {
    // swagger注解，生成swagger文档
    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    // pageParams是分页参数，queryCourseParamsDto是查询条件
    // json数据转为对象需要添加@RequestBody
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        // 1.调用service查询课程信息
        // 2.构建返回结果
        // 3.返回结果
    return null;
    }
}
