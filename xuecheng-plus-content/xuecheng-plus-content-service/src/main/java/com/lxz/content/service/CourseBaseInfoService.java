package com.lxz.content.service;

import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.content.model.dto.AddCourseDto;
import com.lxz.content.model.dto.CourseBaseInfoDto;
import com.lxz.content.model.dto.EditCourseDto;
import com.lxz.content.model.dto.QueryCourseParamsDto;
import com.lxz.content.model.po.CourseBase;

/**
 * PageParams是分页参数
 * QueryCourseParamsDto是查询条件
 */
public interface CourseBaseInfoService {
    // 课程分页查询
    // 返回值为Controller返回的对象
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto);

    // companyId：用户所属机构id
    // addCourseDto：新增课程信息
    // 返回值为课程添加成功的详细信息
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto);

    // 根据课程id查询课程信息
    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    // 修改课程信息
    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);
}
