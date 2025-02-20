package com.xiaosenho.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.media.model.po.MediaProcess;

import java.util.List;

/**
 * 媒资待处理服务
 */
public interface MediaProcessService extends IService<MediaProcess> {
    /**
     * 获取待处理媒资列表，基于乐观锁机制避免重复分配
     * @param shardIndex 当前分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex,int shardTotal,int count);

    /**
     *  开启一个任务,基于乐观锁机制避免重复分配
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);

    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

}
