package com.xiaosenho.content;

import com.xiaosenho.content.config.MultipartSupportConfig;
import com.xiaosenho.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @author: 作者
 * @create: 2025-02-22 15:24
 * @Description:
 */
@SpringBootTest
public class feignTest {
    @Autowired
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() {

        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\project\\xiaosen-School-fore\\xc-ui-pc-static-portal\\course\\course_template.html"));
        mediaServiceClient.uploadFile(multipartFile,"course/test.html");
    }

}
