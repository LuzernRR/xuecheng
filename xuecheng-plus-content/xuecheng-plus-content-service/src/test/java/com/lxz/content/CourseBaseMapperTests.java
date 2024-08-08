package com.lxz.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.content.mapper.CourseBaseMapper;
import com.lxz.content.model.dto.QueryCourseParamsDto;
import com.lxz.content.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBaseMapperTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    public void testCourseBaseMapper(){
        CourseBase courseBase = courseBaseMapper.selectById(18);
        Assertions.assertNotNull(courseBase);
        // ��ϸ���з�ҳ��ѯ�ĵ�Ԫ���ԡ�
        // ��ѯ����
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        courseParamsDto.setCourseName("java"); // �γ����Ʋ�ѯ����
        // ƴװ��ѯ����
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        // ��������ģ����ѯ�� ��sql��ƴ��courseName like '%java%'
        queryWrapper.like(StringUtils.isNotBlank(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        // ���ݿγ����״̬��ѯ�� ��sql��ƴ��audit_status = ��
        queryWrapper.eq(StringUtils.isNotBlank(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        // ��ҳ��������
        PageParams pageParams = new PageParams();
        pageParams.setPageNo(1L);
        pageParams.setPageSize(10L);

        // ����page��ҳ�������󣬵�ǰҳ1��ÿҳ��ʾ10��
         Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // ִ�з�ҳ��ѯ
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // �����б�
        List<CourseBase> items = pageResult.getRecords();
        // �ܼ�¼��
        long total = pageResult.getTotal();
        // List<T> items long counts long page long pageSize
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
        System.out.println(courseBasePageResult);
    }
}
