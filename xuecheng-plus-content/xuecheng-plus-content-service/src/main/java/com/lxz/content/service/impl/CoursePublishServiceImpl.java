package com.lxz.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.content.config.MultipartSupportConfig;
import com.lxz.content.feignclient.MediaServiceClient;
import com.lxz.content.mapper.CourseBaseMapper;
import com.lxz.content.mapper.CourseMarketMapper;
import com.lxz.content.mapper.CoursePublishMapper;
import com.lxz.content.mapper.CoursePublishPreMapper;
import com.lxz.content.model.dto.CourseBaseInfoDto;
import com.lxz.content.model.dto.CoursePreviewDto;
import com.lxz.content.model.dto.TeachplanDto;
import com.lxz.content.model.po.*;
import com.lxz.content.service.CourseBaseInfoService;
import com.lxz.content.service.CoursePublishService;
import com.lxz.content.service.TeachplanService;
import com.lxz.messagesdk.model.po.MqMessage;
import com.lxz.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.print.ServiceUI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/14 上午9:57
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {

    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        // 查询课程基本信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        // 查询课程计划信息
        List<TeachplanDto> teachplanList = teachplanService.findTeachplanTree(courseId);
        coursePreviewDto.setTeachplans(teachplanList);
        return coursePreviewDto;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            // 课程不存在
            XueChengPlusException.cast("课程不存在");
        }
        // todo本机构的课程才允许提交
        // =======如果课程的审核状态为已提交则不允许提交=============
        if (courseBaseInfo.getStatus().equals("202003")) {
            XueChengPlusException.cast("课程已经提交审核");
        }
        // =========课程的图片、计划信息没有填写也不允许提交===========
        if (StringUtils.isEmpty(courseBaseInfo.getPic())) {
            XueChengPlusException.cast("课程图片未上传");
        }
        // ===========查询到课程的基本信息、营销信息、课程计划等信息插入到课程预发布表===========
        // 课程计划信息
        List<TeachplanDto> teachplanList = teachplanService.findTeachplanTree(courseId);
        if (teachplanList == null || teachplanList.size() == 0) {
            XueChengPlusException.cast("课程计划未填写");
        }
        // =========插入预发布表============
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);
        // 营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        // 转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        // 课程计划
        String teachplanJson = JSON.toJSONString(teachplanList);
        coursePublishPre.setTeachplan(teachplanJson);
        coursePublishPre.setStatus("202003");
        // 提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreOld = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreOld != null) {
            // 更新
            coursePublishPreMapper.updateById(coursePublishPre);
        } else {
            // 插入
            coursePublishPreMapper.insert(coursePublishPre);
        }
        // =========更新课程基本信息表的审核状态为已提交===========
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003"); // 审核状态为已提交
        courseBaseMapper.updateById(courseBase);
    }

    @Override
    @Transactional
    public void publish(Long companyId, Long courseId) {
        // =======查询预发布表，向课程发布表写数据===========
        // 课程没有审核通过，不允许发布
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (!coursePublishPre.getStatus().equals("202004")) {
            XueChengPlusException.cast("课程未通过审核，不能发布");
        }
        // 向课程发布表插入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        // 查询课程发布表，有记录则更新，没有则插入
        CoursePublish coursePublishOld = coursePublishMapper.selectById(courseId);
        if (coursePublishOld != null) {
            // 更新
            coursePublishMapper.updateById(coursePublish);
        } else {
            // 插入
            coursePublishMapper.insert(coursePublish);
        }

        // =============向消息表写入数据===============
        saveCoursePublishMessage(courseId);


        // =========将课程预发布表的数据删除==============
        coursePublishPreMapper.deleteById(courseId);
    }

    @Override
    public File generateCourseHtml(Long courseId){
        // 返回的静态文件
        File htmlFile = null;
        // 生成静态化页面
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            // 拿到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            // 设置模板路径
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            // 指定编码
            configuration.setDefaultEncoding("utf-8");
            // 得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            // 准备数据
            CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);
            // 使用map作为数据模型
            HashMap<Object, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);
            // 静态化
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

            // ===========使用流将html写入文件================
            //  输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            htmlFile = File.createTempFile("coursepublish", ".html");
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            // 写入文件
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error("生成课程静态化页面失败,课程id:{}", courseId);
            e.printStackTrace();
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
        return htmlFile;
    }

    @Override
    public void uploadHtmlToMinio(Long courseId, File htmlFile) {
        try {
            // 将file类型转为MultipartFile类型
            File file = new File("D:\\Minio\\LocalData\\HTML\\1.html");
            // 调用方法将file转为MultipartFile
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (upload == null) {
                log.debug("远程调用走降级的逻辑，得到上传的结果为null，课程id:{}", courseId);
                XueChengPlusException.cast("上传html文件到minio失败");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", courseId.toString(), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast("向消息表写入数据失败");
        }
    }
}
