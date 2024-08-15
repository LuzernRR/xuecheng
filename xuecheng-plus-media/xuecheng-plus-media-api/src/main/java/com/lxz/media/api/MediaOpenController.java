package com.lxz.media.api;

import com.lxz.base.model.RestResponse;
import com.lxz.base.utils.StringUtil;
import com.lxz.media.model.po.MediaFiles;
import com.lxz.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/14 上午10:44
 */
@Api(value = "媒资管理接口", tags = "媒资管理接口，提供媒资文件的上传、处理、查询等操作")
@RestController
@RequestMapping("/open")
public class MediaOpenController {
    @Autowired
    MediaFileService mediaFileService;

    @ApiOperation("预览文件")
    @GetMapping("preview/{mediaId")
    public RestResponse<String> getPlayUrlMediaId(@PathVariable String mediaId) {
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null) {
            return RestResponse.validfail("文件不存在");
        }
        // 取出文件的url
        String url = mediaFiles.getUrl();
        if (StringUtil.isEmpty(url)) {
            return RestResponse.validfail("文件地址为空");
        }
        return RestResponse.success(url);
    }
}
