package com.xiaosenho.media.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.media.mapper.MediaFilesMapper;
import com.xiaosenho.media.mapper.MediaProcessHistoryMapper;
import com.xiaosenho.media.mapper.MediaProcessMapper;
import com.xiaosenho.media.model.po.MediaFiles;
import com.xiaosenho.media.model.po.MediaProcess;
import com.xiaosenho.media.model.po.MediaProcessHistory;
import com.xiaosenho.media.service.MediaProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-20 13:36
 * @Description:
 */
@Service
public class MediaProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess> implements MediaProcessService {
    @Resource
    private MediaProcessMapper mediaProcessMapper;
    @Resource
    private MediaFilesMapper mediaFilesMapper;
    @Resource
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        return mediaProcessMapper.getMediaProcessList(shardIndex, shardTotal, count);
    }

    @Override
    public boolean startTask(long id) {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        MediaProcess mediaProcess = getById(taskId);
        if(mediaProcess==null){
            ServiceException.cast("任务不存在");
        }
        //任务状态执行失败
        if("3".equals(status)){
            //更新任务状态
            mediaProcess.setStatus(status);
            //错误次数累加
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);
            //设置错误信息
            mediaProcess.setErrormsg(errorMsg);
            //更新数据库
            updateById(mediaProcess);
        }
        //任务状态执行成功
        if("2".equals(status)){
            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
            if(mediaFiles==null){
                ServiceException.cast("文件不存在");
            }
            //更新媒资访问url
            mediaFiles.setUrl(url);
            mediaFilesMapper.updateById(mediaFiles);

            //更新任务状态
            mediaProcess.setStatus(status);
            mediaProcess.setUrl(url);
            mediaProcess.setFinishDate(LocalDateTime.now());
            //更新数据库
            updateById(mediaProcess);

            //创建已完成任务信息
            MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
            BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
            mediaProcessHistory.setId(null);
            //保存数据库
            mediaProcessHistoryMapper.insert(mediaProcessHistory);

            //从待处理列表中移除任务
            removeById(taskId);
        }
    }
}
