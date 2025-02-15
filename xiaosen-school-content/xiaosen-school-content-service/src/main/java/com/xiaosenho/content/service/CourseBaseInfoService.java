package com.xiaosenho.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.model.po.CourseCategory;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface CourseBaseInfoService extends IService<CourseBase> {
    /**
     * 课程信息分页查询
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
