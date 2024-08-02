package com.lxz.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.content.mapper.CourseBaseMapper;
import com.lxz.content.mapper.CourseCategoryMapper;
import com.lxz.content.model.dto.CourseCategoryTreeDto;
import com.lxz.content.model.dto.QueryCourseParamsDto;
import com.lxz.content.model.po.CourseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTests {

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseBaseMapper(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
