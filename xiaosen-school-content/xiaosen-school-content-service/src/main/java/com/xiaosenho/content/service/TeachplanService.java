package com.xiaosenho.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.content.model.dto.SaveTeachplanDto;
import com.xiaosenho.content.model.dto.TeachPlanDto;
import com.xiaosenho.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2025-02-13
 */
public interface TeachplanService extends IService<Teachplan> {
    /**
     * 根据课程id查询课程计划树形结构
     * @param courseId
     * @return
     */
    public List<TeachPlanDto> getTeachPlanTreeNodesById(Long courseId);

    /**
     * 根据有无课程计划id新增或修改课程计划
     * @param teachPlanDto
     */
    public void saveTeachPlan(SaveTeachplanDto teachPlanDto);

    /**
     * 删除课程计划
     * @param teachPlanId
     */
    void deleteTeachPlan(Long teachPlanId);

    /**
     * 向下移动
     * @param teachPlanId
     */
    void moveDown(Long teachPlanId);

    /**
     * 向上移动
     * @param teachPlanId
     */
    void moveUp(Long teachPlanId);
}
