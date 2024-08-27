package com.lxz.content.api;

import com.alibaba.fastjson.JSON;
import com.lxz.content.model.dto.CourseBaseInfoDto;
import com.lxz.content.model.dto.CoursePreviewDto;
import com.lxz.content.model.dto.TeachplanDto;
import com.lxz.content.model.po.CoursePublish;
import com.lxz.content.service.CoursePublishService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/14 上午9:32
 */
@Controller
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId){
        // CoursePreviewDto中需要添加courseBase、teachplans
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 查询课程发布表
        CoursePublish coursepublish = coursePublishService.getCoursepublish(courseId);
        if (coursepublish == null) {
            return coursePreviewDto;
        }
        // 向dto中设置数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursepublish, courseBaseInfoDto);
        // 课程计划信息
        String teachplan = coursepublish.getTeachplan();
        // 转成List<TeachplanDto>
        List<TeachplanDto> teachplanDtos = JSON.parseArray(teachplan, TeachplanDto.class);
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachplanDtos);
        return coursePreviewDto;
    }

    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId){
        ModelAndView modelAndView = new ModelAndView();
        // 查询课程信息作为模型数据
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId, courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId){
        Long companyId = 1232141425L;
        coursePublishService.publish(companyId, courseId);
    }

    @ApiOperation("查询课程发布信息")
    @ResponseBody
    @GetMapping("/r/coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId){
        // 查询课程发布信息
        return coursePublishService.getCoursepublish(courseId);
    }
}
