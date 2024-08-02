package com.lxz.content.api;

import com.lxz.content.model.dto.CourseCategoryTreeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/1 下午8:37
 */
@RestController
public class CourseCategoryController {
    @GetMapping("course-category/tree-nodes")
    public CourseCategoryTreeDto queryTreeNodes(){
        return null;
    }
}
