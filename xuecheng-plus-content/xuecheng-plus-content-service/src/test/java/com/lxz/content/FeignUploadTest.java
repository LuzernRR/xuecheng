package com.lxz.content;

import com.lxz.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.lxz.content.config.MultipartSupportConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/15 下午4:08
 */
@SpringBootTest
@MapperScan("com.lxz.messagesdk.mapper")
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Test
    public void test() throws IOException {
        // 将file类型转为MultipartFile类型
         File file = new File("D:\\Minio\\LocalData\\HTML\\18.html");
         // 调用方法将file转为MultipartFile
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        mediaServiceClient.upload(multipartFile, "course/18.html");

    }
}
