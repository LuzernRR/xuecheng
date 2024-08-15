package com.lxz.content.jobhandler;

/**
 * @description:课程发布的任务类
 * @author: 12860
 * @time: 2024/8/15 上午11:36
 */

import com.lxz.base.exception.XueChengPlusException;
import com.lxz.content.service.CoursePublishService;
import com.lxz.messagesdk.model.po.MqMessage;
import com.lxz.messagesdk.service.MessageProcessAbstract;
import com.lxz.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
// 使用xxl-job调度任务，执行minio上传，elasticsearch保存，redis缓存
// 继承MessageProcessAbstract抽象类，实现execute方法，就可以实现消息处理
public class CoursePublishTask extends MessageProcessAbstract {

    @Autowired
    CoursePublishService coursePublishService;
    // 任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler(){
        // =======拿到分片参数==============
        int shardIndex = XxlJobHelper.getShardIndex();  // 执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();  // 执行器总数
        // 调用process方法执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    @Override
    public boolean execute(MqMessage mqMessage) {
        // 从mqMessage中获取课程id
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());
        // =================课程静态化上传到minio（stage1）======================
        generateCourseHtml(mqMessage, courseId);
        // ==========向elasticsearch中保存课程信息（stage2）===============
        saveCOurseIndex(mqMessage, courseId);
        // ==============向redis写缓存（stage3）=========================

        return true;  // 任务完成
    }

    // 生成课程静态化页面并上传至文件系统
    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        Long taskId = mqMessage.getId();
        MqMessageService messageService = this.getMqMessageService();
        // ========做任务幂等性处理（检查是否已经执行）==========
        // 取出该阶段执行状态
        int stageOne = messageService.getStageOne(taskId);
        if (stageOne > 0) {
            // 任务已经执行过了
            log.debug("课程静态化任务已经执行");
            return;
        }
        // 对课程生成静态化html页面
        File file = coursePublishService.generateCourseHtml(courseId);
        if (file == null) {
            XueChengPlusException.cast("生成课程静态化页面失败");
        }
        // 上传到minio
        coursePublishService.uploadHtmlToMinio(courseId, file);
        // 任务处理完成后，更新任务状态
        messageService.completedStageOne(taskId);
    }
    // 向elasticsearch中保存课程信息
    private void saveCOurseIndex(MqMessage mqMessage, Long courseId) {
        // 任务幂等性处理
        Long taskId = mqMessage.getId();
        MqMessageService messageService = this.getMqMessageService();
        // 取出该阶段执行状态
        int stageTwo = messageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            // 任务已经执行过了
            log.debug("课程索引任务已经执行");
            return;
        }
        // 查询课程信息，调用搜索服务保存课程信息

        // 任务处理完成后，更新任务状态
        messageService.completedStageTwo(taskId);
    }
}
