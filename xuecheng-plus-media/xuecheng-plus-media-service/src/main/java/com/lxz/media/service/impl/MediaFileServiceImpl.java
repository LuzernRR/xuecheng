package com.lxz.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.lxz.base.exception.XueChengPlusException;
import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.base.model.RestResponse;
import com.lxz.media.mapper.MediaFilesMapper;
import com.lxz.media.mapper.MediaProcessMapper;
import com.lxz.media.model.dto.QueryMediaParamsDto;
import com.lxz.media.model.dto.UploadFileParamsDto;
import com.lxz.media.model.dto.UploadFileResultDto;
import com.lxz.media.model.po.MediaFiles;
import com.lxz.media.service.MediaFileService;
import com.lxz.media.model.po.MediaProcess;
import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Autowired  // autowired注入的都是代理对象， this是原始对象
    MediaFileService currentProxy;  // 注入代理对象才能调用事务方法addMediaFilesToDb

    //拿到nacos中配置的桶名字
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    //拿到nacos中配置的视频名字
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;
    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    // ==================查询媒体文件列表====================
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    //根据扩展名获取mimeType
    private String getMimeType(String extension){
        if(extension == null){
            extension = "";
        }
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;

    }

    /**
     * 将文件上传到minio
     * @param localFilePath 文件本地路径
     * @param mimeType 媒体类型
     * @param bucket 桶
     * @param objectName 对象名
     * @return
     */
    // ==================上传文件到minio====================
    public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName){
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)//桶
                    .filename(localFilePath) //指定本地文件路径
                    .object(objectName)//对象名 放在子目录下
                    .contentType(mimeType)//设置媒体文件类型
                    .build();
            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
            return true;
        } catch (Exception e) {
           e.printStackTrace();
           log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
        }
        return false;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 将年-月-日格式化为年/月/日
        String folder = sdf.format(new Date()).replace("-", "/")+"/";
        return folder;
    }
    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    // ==================上传文件(未分块)====================
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {

        //拿到文件名
        String filename = uploadFileParamsDto.getFilename();
        //就得到扩展名
        String extension = filename.substring(filename.lastIndexOf("."));

        //得到mimeType
        String mimeType = getMimeType(extension);

        // 根据当前时间生成默认的文件夹路径
        String defaultFolderPath = getDefaultFolderPath();
        // 获取文件的md5
        String fileMd5 = getFileMd5(new File(localFilePath));
        // objectName：文件夹日期路径+md5文件名+扩展名
        String objectName = defaultFolderPath+fileMd5+extension;
        //上传文件到minio，返回boolean
        boolean result = addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if(!result){
            XueChengPlusException.cast("上传文件失败");
        }
        //入库文件信息
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);
        if(mediaFiles==null){
            XueChengPlusException.cast("文件上传后保存信息失败");
        }
        //准备返回的对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        // 将mediaFiles的属性拷贝到uploadFileResultDto
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }


    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     * @author Mr.M
     * @date 2022/10/12 21:22
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //将文件信息保存到数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
            //文件id
            mediaFiles.setId(fileMd5);
            //机构id
            mediaFiles.setCompanyId(companyId);
            //桶
            mediaFiles.setBucket(bucket);
            //file_path
            mediaFiles.setFilePath(objectName);
            //file_id
            mediaFiles.setFileId(fileMd5);
            //url
            mediaFiles.setUrl("/"+bucket+"/"+objectName);
            //上传时间
            mediaFiles.setCreateDate(LocalDateTime.now());
            //状态
            mediaFiles.setStatus("1");
            //审核状态, 002003表示不需要审核
            mediaFiles.setAuditStatus("002003");
            //插入数据库
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert<=0){
                log.debug("向数据库保存文件失败,bucket:{},objectName:{}",bucket,objectName);
                return null;
            }
            // ======记录待处理任务：向media_process中插入记录=========
            // 通过mimetype判断文件类型，如果是avi视频则写入待处理任务
            addWaitingTask(mediaFiles);
            return mediaFiles;
        }



        return mediaFiles;

    }

    // ===============记录待处理任务：向media_process中插入记录=========
    private void addWaitingTask(MediaFiles mediaFiles) {
        // 获取文件的mimeType
        String mimeType = mediaFiles.getFilename();  // 文件名
        String extension = mimeType.substring(mimeType.lastIndexOf("."));  // 文件扩展名
        String mimeType1 = getMimeType(extension);    // 得到文件的mimeType
        // 判断mime类型是否是avi
        if (mimeType1.equals("video/x-msvideo")){  // 如果是avi则写入待处理任务
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setFileId(mediaFiles.getFileId());
            mediaProcess.setFilename(mediaFiles.getFilename());
            // 设置状态未处理
            mediaProcess.setStatus("1");  // 1未处理，2成功，3失败
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);  // 失败次数默认0
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    @Override
    // ===============检查数据库中是否存在文件=======================
    public RestResponse<Boolean> checkFile(String fileMd5) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles!=null){
            // 拿到文件所在的捅
            String bucket = mediaFiles.getBucket();
            // 拿到objectName
            String filePath = mediaFiles.getFilePath();
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (ErrorResponseException e) {
                e.printStackTrace();
                log.error("文件不存在,bucket:{},objectName:{}",bucket,filePath);
            }
        }
        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    // ==================检查分块文件是否存在====================
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 分块文件的存储目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFileFolderPath + chunkIndex)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null){
                // 分块文件存在
                return RestResponse.success(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    private String getChunkFileFolderPath(String fileMD5){
        return fileMD5.substring(0, 1) + "/" + fileMD5.substring(1, 2) + "/" + fileMD5 + "/" + "chunk" + "/";
    }
    private String getFilePathByMD5(String fileMD5, String fileExt){
        return fileMD5.substring(0, 1) + "/" + fileMD5.substring(1, 2) + "/" + fileMD5 + fileExt;
    }
    @Override
    // ==================上传分块文件====================
    public RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath) {
        // 获取MimeType
        String MimeType = getMimeType(null);
        // 分块文件的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5) + chunk;
        // 将文件上传到minion
        boolean b = addMediaFilesToMinIO(localFilePath, MimeType, bucket_video, chunkFileFolderPath);
        if (!b){
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    // 合并分块文件后写入数据库
    public RestResponse mergechunks(long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // ========找到所有的分块文件，调用minio的sdk进行文件合并===============
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal).map(i ->
                ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath + i)  // 指定分块文件的路径
                        .build()).collect(Collectors.toList());

        // composeObject中需要传入一个ComposeObjectArgs对象
        // 源文件名称
        String filename = uploadFileParamsDto.getFilename();
        // 扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 合并后文件的objectName
        String filePathByMD5 = getFilePathByMD5(fileMd5, extension);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)  // 指定传入到哪个桶
                .object(filePathByMD5)  // 指定合并后的文件路径md5+扩展名
                .sources(sources)           // 指定需要合并的文件
                .build();
        // minio的分块文件必须大于5M，否则会报错size 1048576 must be greater than 5242880
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (ErrorResponseException e) {
            e.printStackTrace();
            log.error("合并分块文件失败,bucket:{},objectName:{}",bucket_video,filePathByMD5);
            return RestResponse.validfail(false, "合并分块文件失败");
        }
        // ===========校验合并后的文件和源文件是否一致，视频上传才成功===============
        // 获取minio中文件的md5
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(filePathByMD5)
                .build();
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        String source_md5 = DigestUtils.md5Hex(inputStream);
        if (!source_md5.equals(fileMd5)){
            log.error("合并后的文件和源文件md5不一致,bucket:{},objectName:{}",bucket_video,filePathByMD5);
            return RestResponse.validfail(false, "合并后的文件和源文件md5不一致");
        }
        // ==========将文件信息入库===============
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, filePathByMD5);
        if (mediaFiles == null){
            log.error("合并后的文件信息入库失败,bucket:{},objectName:{}",bucket_video,filePathByMD5);
            return RestResponse.validfail(false, "合并后的文件信息入库失败");
        }
        // ==============清理分块文件===================
        for (int i = 0; i < chunkTotal; i++) {
            String chunkFilePath = chunkFileFolderPath + i;
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(chunkFilePath)
                    .build();
            minioClient.removeObject(removeObjectArgs);
        }
        return RestResponse.success(true);
    }
}
