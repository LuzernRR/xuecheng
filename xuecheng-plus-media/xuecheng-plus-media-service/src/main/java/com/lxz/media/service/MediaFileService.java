package com.lxz.media.service;

import com.lxz.base.model.PageParams;
import com.lxz.base.model.PageResult;
import com.lxz.base.model.RestResponse;
import com.lxz.media.model.dto.QueryMediaParamsDto;
import com.lxz.media.model.dto.UploadFileParamsDto;
import com.lxz.media.model.dto.UploadFileResultDto;
import com.lxz.media.model.po.MediaFiles;
import io.minio.errors.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

 /**
  * 上传文件
  * @param companyId 机构id
  * @param uploadFileParamsDto 文件信息
  * @param localFilePath 文件本地路径
  * @return UploadFileResultDto
  */
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);

 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

 RestResponse<Boolean> checkFile(String fileMd5) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

 RestResponse<Boolean> checkChunk(String fileMd5, int chunk) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

 RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath);

 RestResponse mergechunks(long l, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
