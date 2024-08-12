package com.lxz.media.service.impl;

import com.lxz.media.mapper.MediaProcessMapper;
import com.lxz.media.model.po.MediaProcess;
import com.lxz.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Override
    public List<MediaProcess> getMediaProcessList(int sharedTotal, int sharedIndex, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListBySharedIndex(sharedTotal, sharedIndex, count);
        return mediaProcesses == null ? Collections.emptyList() : mediaProcesses;
    }
}
