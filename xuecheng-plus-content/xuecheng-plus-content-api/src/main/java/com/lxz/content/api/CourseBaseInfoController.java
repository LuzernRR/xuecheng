package com.lxz.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 相当于responsebody+controller
@RestController
// 用于响应json数据
public class CourseBaseInfoController {
    @RequestMapping("/course/list")
    // pageParams是分页参数，queryCourseParamsDto是查询条件
    // json数据转为对象需要添加@RequestBody
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParamsDto) {
        // 1.调用service查询课程信息
        // 2.构建返回结果
        // 3.返回结果
    return null;
    }
}
