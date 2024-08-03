package com.lxz.content;

import com.lxz.content.model.dto.CourseCategoryTreeDto;
import com.lxz.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryServiceTests {
    @Autowired
    private CourseCategoryService courseCategoryService;

    @Test
    public void testCourseBaseMapper(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }
}
