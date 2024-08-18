package com.lxz.content.service;

import com.lxz.content.model.dto.CoursePreviewDto;
import com.lxz.content.model.po.CoursePublish;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

// 课程发布相关的接口

public interface CoursePublishService {
    // 课程预览
    public CoursePreviewDto getCoursePreviewInfo(Long courseId);

    // 提交课程审核结果
    public void commitAudit(Long companyId, Long courseId);

    // 课程发布
    public void publish(Long companyId, Long courseId);

    // 对课程生成静态化html页面
    public File generateCourseHtml(Long courseId);

    // 上传静态页面到minio
    public void uploadHtmlToMinio(Long courseId, File htmlFile);

    CoursePublish getCoursepublish(Long courseId);

    // 
}
