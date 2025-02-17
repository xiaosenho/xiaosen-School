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
    public List<TeachPlanDto> getTeachPlanTreeNodesById(Long courseId);
    public void saveTeachPlan(SaveTeachplanDto teachPlanDto);
}
