package com.xiaosenho.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 作者
 * @create: 2025-02-22 16:02
 * @Description:
 */
public class MediaServiceClientFallback implements MediaServiceClient{
    @Override
    public String uploadFile(MultipartFile upload, String objectName) {
        return null;
    }
}
