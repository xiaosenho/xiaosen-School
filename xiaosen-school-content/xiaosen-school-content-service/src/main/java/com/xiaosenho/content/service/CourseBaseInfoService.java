package com.xiaosenho.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;

public interface CourseBaseInfoService extends IService<CourseBase> {
    /**
     * 课程信息分页查询
     * @param pageParams
     * @param queryCourseParamsDto
     * @return
     */
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);
}
