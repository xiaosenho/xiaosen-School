package com.xiaosenho.media.service.jobhandler;

import com.xiaosenho.base.utils.Mp4VideoUtil;
import com.xiaosenho.media.model.po.MediaProcess;
import com.xiaosenho.media.service.MediaFileService;
import com.xiaosenho.media.service.MediaProcessService;
import com.xiaosenho.media.utils.MinioUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xiaosen
 * @date 2025/2/20 16:51
 * @description: 视频处理任务
 */
@Component
@Slf4j
public class VideoTask {
    private static Logger logger = LoggerFactory.getLogger(VideoTask.class);

    @Resource
    private MediaProcessService mediaProcessService;
    @Resource
    private MinioUtil minioUtil;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 分片广播任务
     */
    @XxlJob("videoTaskHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();//执行器序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal();//执行器总数，从1开始

        // 获取cpu核心数，即可并发任务数
        int cpuCoreCount = Runtime.getRuntime().availableProcessors();

        // 根据cpu核心数查询最大待处理任务列表
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, cpuCoreCount);
        int size = mediaProcessList.size();
        if (size == 0) {
            log.debug("没有待处理的任务");
            return;
        }
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);

        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            executorService.execute(() -> {

                // 开启任务，获取锁
                Long taskId = mediaProcess.getId();
                boolean lock = mediaProcessService.startTask(taskId);
                if (!lock){
                    log.debug("任务已被其他执行器执行，taskId:{}", taskId);
                    return;
                }
                // 下载视频
                String fileId = mediaProcess.getFileId();
                String bucket = mediaProcess.getBucket();
                String objectName = mediaProcess.getFilePath();
                try {
                    File file = minioUtil.getFile(bucket, objectName);
                    if(file==null){
                        log.debug("下载视频失败，taskId:{}", taskId);
                        // 保存失败信息
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId,null, "下载视频失败");
                        return;
                    }
                    // 执行任务，视频转码
                    // 创建工具类
                    String videoPath = file.getAbsolutePath();//下载视频源路径
                    String videoName = mediaProcess.getFileId()+".mp4";//无用，只是占位
                    File mp4Video = null;//转码后视频临时文件
                    try {
                        mp4Video = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件失败，taskId:{}", taskId);
                        // 保存失败信息
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId,null, "创建临时文件失败");
                        return;
                    }
                    String mp4VideoAbsolutePath = mp4Video.getAbsolutePath();//临时文件地址
                    // 执行转码
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath,videoPath,videoName,mp4VideoAbsolutePath);
                    String result = mp4VideoUtil.generateMp4();
                    if(!"success".equals(result)){
                        log.debug("视频转码失败，taskId:{}", taskId);
                        // 保存失败信息
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId,null, result);
                        return;
                    }
                    // 上传视频
                    String defaultFolderPath = minioUtil.getChunkFileFolder(fileId);
                    String newObjectName = defaultFolderPath + fileId + ".mp4";//新文件名
                    boolean upload = minioUtil.upload(bucket, newObjectName, mp4VideoAbsolutePath, "video/mp4");
                    if(!upload){
                        log.debug("上传视频失败，taskId:{}", taskId);
                        // 保存失败信息
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId,null, "上传视频失败");
                        return;
                    }
                    // 保存任务处理结果
                    String url = "/"+ bucket +"/"+ newObjectName;// 转码后新视频url
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }catch (Exception e){
                    log.debug("视频处理失败，taskId:{}", taskId);
                    // 保存失败信息
                    mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId,null, "视频处理失败");
                } finally {
                    countDownLatch.countDown();
                }
            });
        });

        // 等待所有任务执行完成
        countDownLatch.await();
    }

}
