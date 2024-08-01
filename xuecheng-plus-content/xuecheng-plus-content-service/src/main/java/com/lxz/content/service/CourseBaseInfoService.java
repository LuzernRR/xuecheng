package com.lxz.content.service;

import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
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
}
