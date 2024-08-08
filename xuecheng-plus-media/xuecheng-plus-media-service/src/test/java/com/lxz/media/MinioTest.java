package com.lxz.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试minio的sdk
 * @date 2023/2/17 11:55
 */
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://127.0.0.1:9005")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void test_upload() throws Exception {

        //通过扩展名得到媒体资源类型 mimeType
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".png");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        // 如果extensionMatch不为空，则将mimeType设置为extensionMatch的mimeType
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }

        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")//桶
                .filename("E:\\03控制工程\\研二上\\AIDA\\专利\\Figure_1.png") //指定本地文件路径
//                .object("1.mp4")//对象名 在桶下存储该文件
                .object("test/01/Figure_1.png")//对象名 放在子目录下
                .contentType(mimeType)//设置媒体文件类型
                .build();

        //上传文件
        minioClient.uploadObject(uploadObjectArgs);



    }
    //删除文件
    @Test
    public void test_delete() throws Exception {

        //RemoveObjectArgs
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("test/01/Figure_1.png")
                .build();

        //删除文件
        minioClient.removeObject(removeObjectArgs);



    }

    //查询文件 从minio中下载
    @Test
    public void test_getFile() throws Exception {

        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("test/01/Figure_1.png")
                .build();
        //查询远程服务获取到一个流对象
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        //指定输出流（下载的路径）
        FileOutputStream outputStream = new FileOutputStream(new File("E:\\03控制工程\\研二上\\AIDA\\专利\\Figure_2.png"));
        IOUtils.copy(inputStream,outputStream);

        //校验文件的完整性对文件的内容进行md5
        FileInputStream fileInputStream1 = new FileInputStream(new File("E:\\03控制工程\\研二上\\AIDA\\专利\\Figure_1.png"));
        String source_md5 = DigestUtils.md5Hex(fileInputStream1);  // minio文件的md5
        FileInputStream fileInputStream = new FileInputStream(new File("E:\\03控制工程\\研二上\\AIDA\\专利\\Figure_2.png"));
        String local_md5 = DigestUtils.md5Hex(fileInputStream);  // 下载的文件的md5
        if(source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }

    }









}
