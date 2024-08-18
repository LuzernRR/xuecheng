package com.lxz.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.content.mapper.CourseBaseMapper;
import com.lxz.content.model.dto.QueryCourseParamsDto;
import com.lxz.content.model.po.CourseBase;
import com.lxz.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBaseInfoServiceTests {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Test
    public void testCourseBaseMapper(){
        // ��ѯ����
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java"); // �γ����Ʋ�ѯ����
        courseParamsDto.setAuditStatus("202001"); // ���״̬��ѯ����
        // ��ҳ��������
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(10L);
        // ��ѯ�γ���Ϣ
        Long companyId = 1L;
        // ����service��ѯ�γ���Ϣ
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(companyId, pageParams, courseParamsDto);
        System.out.println(courseBasePageResult);
    }
}
