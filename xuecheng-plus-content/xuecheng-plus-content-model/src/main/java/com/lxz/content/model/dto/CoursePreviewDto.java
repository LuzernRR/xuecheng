package com.lxz.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/14 上午9:51
 */
@Data
public class CoursePreviewDto {
    // 课程基本信息、营销信息
    private CourseBaseInfoDto courseBase;

    // 课程计划信息
    private List<TeachplanDto> teachplans;

    // 课程师资信息
}
