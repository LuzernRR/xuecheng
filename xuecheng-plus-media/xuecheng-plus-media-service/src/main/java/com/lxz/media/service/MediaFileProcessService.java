package com.lxz.media.service;

import com.lxz.media.model.po.MediaProcess;

import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/12 下午10:11
 */
public interface MediaFileProcessService {
    public List<MediaProcess> getMediaProcessList(int sharedTotal, int sharedIndex, int count);
}
