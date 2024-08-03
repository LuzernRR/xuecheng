package com.lxz.content.model.dto;

import com.lxz.content.model.po.CourseCategory;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/1 下午8:24
 */
@Data
// Serializable是java.io包下的，是序列化接口，实现这个接口的类可以被序列化
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    // 父类没有给出下级节点childrenTreeNodes，需要定义一个列表存放多个下级节点
    List<CourseCategoryTreeDto> childrenTreeNodes;

}
