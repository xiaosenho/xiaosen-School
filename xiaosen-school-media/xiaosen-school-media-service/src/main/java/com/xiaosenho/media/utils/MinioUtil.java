package com.xiaosenho.media.utils;

import com.xiaosenho.base.exception.ServiceException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            UploadObjectArgs.Builder uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath);
            if(mimeType!=null) uploadObjectArgs.contentType(mimeType);
            minioClient.uploadObject(uploadObjectArgs.build());
            return true;
        } catch (Exception e) {
            log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucketName,objectName,e.getMessage(),e);
            return false;
        }
    }

    public StatObjectResponse exist(String bucketName, String objectName) {
        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName).build();
            StatObjectResponse statObjectResponse = minioClient.statObject(statObjectArgs);
            return statObjectResponse;//文件存在
        } catch (Exception e) {
            return null;//抛出异常，文件不存在
        }
    }

    public boolean merge(String bucketName, String chunkFolder, String objectName, int chunkTotal) {
        List<ComposeSource> composeSources = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucketName)
                    .object(chunkFolder+i)//分块目录
                    .build();
            composeSources.add(composeSource);
        }
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)//目标文件名
                .sources(composeSources)//分块源文件参数
                .build();
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.composeObject(composeObjectArgs);
            return true;
        } catch (Exception e) {
            log.error("合并文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucketName,objectName,e.getMessage(),e);
            return false;
        }
    }

    public void removeChunk(String bucketName, String chunkFileFolder, int chunkTotal) {
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolder.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(deleteObjects)
                    .build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(result -> {//要想真正删除，需要遍历所有结果，并处理异常
                try {
                    DeleteError deleteError = result.get();
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            });
        } catch (Exception e) {
            log.error("删除分块文件出错,bucket:{},objectName:{},错误原因:{}",bucketName,chunkFileFolder,e.getMessage(),e);
        }
    }

    public File getFile(String bucketName, String objectName) {
        String extension = objectName.substring(objectName.lastIndexOf("."));//文件后缀
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();
            InputStream inputStream = minioClient.getObject(getObjectArgs);
            File file = File.createTempFile("minio", extension);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, fileOutputStream);
            inputStream.close();
            fileOutputStream.close();
            return file;
        } catch (Exception e){
            log.error("从minio获取文件出错,videoFilesBucketName:,objectName:,错误原因:", bucketName, objectName, e.getMessage(), e);
            return null;
        }
    }

    public String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    public String getChunkFileFolder(String fileMd5) {
        String floder1 = String.valueOf(fileMd5.charAt(0));
        String floder2 = String.valueOf(fileMd5.charAt(1));
        return floder1 + "/" + floder2 + "/" ;
    }
}
