package com.xiaosenho.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.po.CourseCategory;

import java.util.List;

/**
 * <p>
 * 课程分类 Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface CourseCategoryMapper extends BaseMapper<CourseCategory> {
    //递归查询分类树
    public List<CourseCategoryTreeDto> selectTreeNodes(String id);
}
