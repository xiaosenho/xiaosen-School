package com.xiaosenho.content.service;

import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.po.CourseCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 课程分类 服务类
 * </p>
 *
 * @author itcast
 * @since 2025-02-13
 */
public interface CourseCategoryService extends IService<CourseCategory> {
    /**
     * 课程分类查询
     * @return
     */
    public List<CourseCategoryTreeDto> queryCourseCategoryList(String id);
}
