package com.lxz.media.service;

import com.lxz.media.model.po.MediaProcess;

import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/12 下午10:11
 */
public interface MediaFileProcessService {
    // 查询待处理任务
    public List<MediaProcess> getMediaProcessList(int sharedTotal, int sharedIndex, int count);

    // 保存处理状态
    void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

    // 乐观锁开启任务，谁抢到谁处理，拿到的返回1
    boolean startTask(Long id);
}
