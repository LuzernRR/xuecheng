package com.lxz.media.service.impl;

import com.lxz.media.mapper.MediaFilesMapper;
import com.lxz.media.mapper.MediaProcessHistoryMapper;
import com.lxz.media.mapper.MediaProcessMapper;
import com.lxz.media.model.po.MediaFiles;
import com.lxz.media.model.po.MediaProcess;
import com.lxz.media.model.po.MediaProcessHistory;
import com.lxz.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/12 下午10:12
 */
@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int sharedTotal, int sharedIndex, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListBySharedIndex(sharedTotal, sharedIndex, count);
        return mediaProcesses == null ? Collections.emptyList() : mediaProcesses;
    }

    // 乐观锁开启任务，谁抢到谁处理，拿到的返回1
    @Override
    public boolean startTask(Long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }


    @Override
    // 保存处理状态
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null) {
            return;
        }
        // 更新处理状态
        // ===============任务失败==============
        // 更新状态为3，并且fail_count+1
        if (status.equals("3")) {
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            // 更高效的方法:mediaProcessMapper.update(mediaProcess, new UpdateWrapper<MediaProcess>().eq("id", taskId));
        }
        // ===========任务成功====================
        // 更新media_file表中的文件url为.mp4
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        // 更新media_process表中的状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);

        // 从media_process表中删除当前任务，并将记录插入到media_process_history中
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);
        // 删除media_process表中的记录
        mediaProcessMapper.deleteById(taskId);
    }
}
