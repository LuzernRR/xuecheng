package com.lxz.content.feignclient;

import com.lxz.media.model.dto.UploadFileResultDto;
import org.springframework.cloud.openfeign.FeignClient;
import com.lxz.content.config.MultipartSupportConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class})
public interface MediaServiceClient {
    @RequestMapping(value = "/media/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String  upload(@RequestPart("filedata") MultipartFile filedata,
                                              @RequestParam(value = "objectName", required = false) String objectName) throws IOException;
    }
