package com.lxz.media;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @description:
 * @author: 12860
 * @time: 2024/8/9 下午8:08
 */
public class BigFileTest {

    // 分块测试
    @Test
    public void testChunk() throws IOException {
        // 源文件
        File sourceFile = new File("D:\\Minio\\LocalData\\1.mp4");
        // 分块文件存储目录
        String chunkFilePath = "D:\\Minio\\LocalData\\chunks\\";
        // 分块文件的大小：5M
        long chunkSize = 5 * 1024 * 1024;
        // 分块数量
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读取数据，向分块文件中写数据, r表示只读
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        // 写入分块文件
        for (int i = 0; i < chunkNum; i++) {
            // 创建分块文件
            File chunkFile = new File(chunkFilePath + i);
            // 分块文件写入流
            RandomAccessFile raf_w = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            // 缓冲区
            byte[] b = new byte[1024];
            // 从源文件中读取数据
            while ((len = raf_r.read(b)) != -1) {
                // 往raf_w中写入，从0开始，写入len长度
                raf_w.write(b, 0, len);
                // 如果写入的数据大小等于分块文件的大小，就不再写入
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }
            raf_w.close();
        }
    }

    // 合并测试,如果和原来文件的md5相等，则通过
    @Test
    public void testMerge() throws IOException {
        // 分块文件目录
        File chunkFolder = new File("D:\\Minio\\LocalData\\chunks\\");
        // 源文件
        File sourceFile = new File("D:\\Minio\\LocalData\\1.mp4");
        // 合并后的文件
        File mergeFile = new File("D:\\Minio\\LocalData\\1_merge.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        // 将数组转成list
         List<File> fileList = Arrays.asList(files);
        // 按照文件名排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                // 如果o1的名字小于o2的名字，返回负数，否则返回正数
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        // 向合并文件写的流
        RandomAccessFile raf_w = new RandomAccessFile(mergeFile, "rw");
        // 遍历分块文件，向合并的文件中写数据
        for (File file : fileList) {
            // 读分块的流
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            byte[] b = new byte[1024];
            while ((len = raf_r.read(b)) != -1) {
                raf_w.write(b, 0, len);
            }
        }
        raf_w.close();

        // 校验文件的完整性,对文件的内容进行md5校验
        FileInputStream fileInputStream1 = new FileInputStream(sourceFile);
        String source_md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream1);  // 源文件的md5
        FileInputStream fileInputStream = new FileInputStream(mergeFile);
        String merge_md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fileInputStream);  // 合并的文件的md5
        assert source_md5.equals(merge_md5);
    }
}
