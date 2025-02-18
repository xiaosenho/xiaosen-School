package com.xiaosenho.media.utils;

import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.media.config.MinioConfig;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: 作者
 * @create: 2025-02-18 16:46
 * @Description:
 */
@Component
@Slf4j
public class MinioUtil {
    @Resource
    private MinioClient minioClient;

    public boolean upload(String bucketName, String objectName, String filePath, String mimeType) {
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)//添加子目录
                    .filename(filePath)
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            return true;
        } catch (Exception e) {
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucketName,objectName,e.getMessage(),e);
            ServiceException.cast("上传文件失败");
        }
        return false;
    }
}
