package com.lxz.media.service.jobhandler;

import com.lxz.base.utils.Mp4VideoUtil;
import com.lxz.media.model.po.MediaProcess;
import com.lxz.media.service.MediaFileProcessService;
import com.lxz.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频任务处理类
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class videoTask {
    private static Logger logger = LoggerFactory.getLogger(videoTask.class);
    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;
    // ffmpeg路径
    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // =======拿到分片参数==============
        int shardIndex = XxlJobHelper.getShardIndex();  // 执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();  // 执行器总数
        // 获取cpu的核心数
        int peocessors = Runtime.getRuntime().availableProcessors();

        // ========查询待处理任务==============
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, peocessors);
        // 任务数量
        int size = mediaProcessList.size();
        if (size == 0) {
            log.debug("没有任务需要处理");
            return;
        }
        // 创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // 使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        // 遍历任务
        mediaProcessList.forEach(mediaProcess -> {
            // 将任务加入线程池
            executorService.execute(() -> {
                try {
                    // 开启任务
                    Long taskId = mediaProcess.getId();
                    String fileId = mediaProcess.getFileId();  // md5
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("任务已被处理，taskId:{}", taskId);
                        return;
                    }
                    // =====抢锁成功，执行视频转码=====
                    String bucket = mediaProcess.getBucket();  // 桶名
                    String objectName = mediaProcess.getFilePath();  // 文件路径
                    // 下载视频文件
                    File file = mediaFileService.downloadFileFromMinio(bucket, objectName);
                    if (file == null){
                        log.error("下载视频出错，bucket:{},filePath:{}", bucket, objectName);
                        // ======保存任务处理失败结果=========
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3",fileId, null, "下载视频出错");
                        return;
                    }

                    //转换后mp4文件的名称
                    String video_path = file.getAbsolutePath();
                    String mp4_name = fileId + ".mp4";
                    //转换后mp4文件的路径
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件失败");
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3",fileId, null, "创建临时文件出错");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath,video_path,mp4_name,mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!result.equals("success")) {
                        // ========视频转码失败，保存任务处理结果=========
                        log.debug("视频转码失败，原因{}，bucket:{},filePath:{}", result, bucket, objectName);
                        // mp4文件的url
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }
                    // ========视频转码成功，上传到minio=========
                    // 上传到minio
                    // 将objectName中的文件名替换为mp4文件名
                    objectName = objectName.substring(0, objectName.lastIndexOf("/") + 1) + mp4_name;
                    boolean b1 = mediaFileService.addMediaFilesToMinIO(mp4_path, "video/mp4", bucket, objectName);
                    if (!b1){
                        log.error("上传视频到minio失败，bucket:{},filePath:{}", bucket, objectName);
                        // ========保存任务处理失败结果=========
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传视频到minio失败");
                        return;
                    }
                    // mp4文件的url
                    String filePathByMD5 = getFilePathByMD5(fileId, ".mp4");
                    // ============保存任务处理结果到数据库=========
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, filePathByMD5, null);
                }finally {
                    // 使用try-finally保证不论是否异常，计数器都会减一
                     countDownLatch.countDown();
                }
                // 计数器减一
            });
        });
        // 计数器阻塞，最多阻塞30分钟后解除阻塞，从而释放线程池
        countDownLatch.await(30, TimeUnit.MINUTES);
    }
    private String getFilePathByMD5(String fileMD5, String fileExt) {
        return fileMD5.substring(0, 1) + "/" + fileMD5.substring(1, 2) + "/" + fileMD5 + fileExt;
    }

}
