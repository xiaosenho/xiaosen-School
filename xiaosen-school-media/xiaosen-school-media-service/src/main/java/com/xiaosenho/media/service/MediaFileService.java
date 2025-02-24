package com.xiaosenho.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.base.model.RestResponse;
import com.xiaosenho.media.model.dto.QueryMediaParamsDto;
import com.xiaosenho.media.model.dto.UploadFileParamsDto;
import com.xiaosenho.media.model.dto.UploadFileResultDto;
import com.xiaosenho.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService extends IService<MediaFiles> {

     /**
      * @description 媒资文件查询方法
      * @param pageParams 分页参数
      * @param queryMediaParamsDto 查询条件
      * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
      * @author Mr.M
      * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);


    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String filepath, String objectName);

    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String filesBucketName, String objectName);

    public RestResponse<Boolean> checkFile(String fileMd5);

    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk);

    public RestResponse uploadchunk(String file, String fileMd5, int chunk);

    public RestResponse mergechunks(Long companyId, String fileMd5, String fileName, int chunkTotal);
}
