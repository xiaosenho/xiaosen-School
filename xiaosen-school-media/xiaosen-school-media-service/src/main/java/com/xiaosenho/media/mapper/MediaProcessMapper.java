package com.xiaosenho.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaosenho.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /**
     * 根据分片获取待处理任务列表
     * @param shardIndex 执行器下标
     * @param shardTotal 执行器总数
     * @param count 每个执行器每次执行任务数
     * @return
     */
    @Select("select * from media_process mp " +
            "where mp.id % #{shardTotal} = #{shardIndex} and (mp.status = 1 or mp.status = 3) limit #{count}")
    public List<MediaProcess> getMediaProcessList(@Param("shardIndex") int shardIndex,@Param("shardTotal") int shardTotal,@Param("count") int count);

    /**
     * 开启一个任务，使用乐观锁机制，将status从1或3更新为4，即执行中状态，
     * 避免执行器断开后执行器总数发生变化，将正在处理但还未修改状态的任务重新分配给其他执行器
     * @param id 任务id
     * @return 更新记录数
     */
    @Update("update media_process m set m.status='4' where (m.status='1' or m.status='3') and m.fail_count<3 and m.id=#{id}")
    //TODO 任务补偿机制，在数据库中添加一个开启时间字段，之后单独启动一个任务找到待处理任务表中超过执行期限（如设为30分钟）但仍在处理中的任务，将任务的状态改为执行失败。
    public int startTask(@Param("id") long id);
}
