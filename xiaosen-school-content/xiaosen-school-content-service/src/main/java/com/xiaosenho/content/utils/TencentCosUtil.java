package com.xiaosenho.content.utils;

/**
 * @author: 作者
 * @create: 2024-12-11 16:07
 * @Description:
 */

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Cos 对象存储操作
 */
@Data
@AllArgsConstructor
@Slf4j
public class TencentCosUtil {

    private String accessKey;

    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    private String filePath;
    private String baseUrl;


    /**
     * 上传对象
     * @param filename 文件名
     * @param file 文件
     * @return
     */
    public String putObject(String filename, File file) {
        // 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 设置bucket的区域
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端
        COSClient cosClient = new COSClient(cred, clientConfig);
        String key=filePath+"/"+filename;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, key, file);
        try {
            cosClient.putObject(putObjectRequest);
        } catch (CosClientException e) {
            throw new RuntimeException(e);
        }
        return baseUrl+key;
    }
}
