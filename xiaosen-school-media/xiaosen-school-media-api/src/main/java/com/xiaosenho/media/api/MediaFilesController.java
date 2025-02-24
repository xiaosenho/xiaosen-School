package com.xiaosenho.media.api;

import com.xiaosenho.base.constant.MediaTypeEnum;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.media.model.dto.QueryMediaParamsDto;
import com.xiaosenho.media.model.dto.UploadFileParamsDto;
import com.xiaosenho.media.model.dto.UploadFileResultDto;
import com.xiaosenho.media.model.po.MediaFiles;
import com.xiaosenho.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @description 媒资文件管理接口
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;


    @ApiOperation("媒资列表查询接口")
    @PostMapping("/files")
    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
        //TODO 登录校验
         Long companyId = 1232141425L;
         return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);
    }

    @ApiOperation("媒资上传接口")
    @PostMapping(value = "/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto upload(@RequestPart("filedata") MultipartFile multipartFile,
                                      @RequestParam(value= "objectName",required=false) String objectName){
        //TODO 登录校验
        Long companyId = 1232141425L;
        try {
            //原始的MultipartFile使用的是InputStream输入流,不能重复读取，也就是说直接对输入流计算md5码和转换成的文件md5码不一致
            //上传文件转成临时文件，目的是计算md5码,并且用于获取上传时本地路径
            File tempFile = File.createTempFile("minio", ".temp");
            multipartFile.transferTo(tempFile);
            String localFilePath = tempFile.getAbsolutePath();
            //文件信息预处理
            UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
            uploadFileParamsDto.setFilename(multipartFile.getOriginalFilename());
            if(StringUtils.isEmpty(objectName)){
                uploadFileParamsDto.setFileType(MediaTypeEnum.IMAGE.getCode());//图片格式
            }else uploadFileParamsDto.setFileType(MediaTypeEnum.OTHER.getCode());//其它格式
            uploadFileParamsDto.setFileSize(multipartFile.getSize());
            //调用业务逻辑方法
            return mediaFileService.uploadFile(companyId,uploadFileParamsDto,localFilePath,objectName);
        } catch (IOException e) {
            ServiceException.cast("上传文件失败");
        }
        return null;
    }

}
