package com.lxz.content;

import com.lxz.content.model.dto.CoursePreviewDto;
import com.lxz.content.service.CoursePublishService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/15 下午3:09
 */
@SpringBootTest
@MapperScan("com.lxz.messagesdk.mapper")
public class FreemarkerTest {

    @Autowired
    CoursePublishService coursePublishService;
    @Test
    public void testGenerateHtmlByTemplate() throws IOException, TemplateException {
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
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(1L);
        // 使用map作为数据模型
        HashMap<Object, Object> map = new HashMap<>();
        map.put("model", coursePreviewInfo);
        // 静态化
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);

        // ===========使用流将html写入文件================
        //  输入流
        InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\Minio\\LocalData\\HTML\\1.html"));
        // 写入文件
        IOUtils.copy(inputStream, outputStream);
    }
}
