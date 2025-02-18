package com.xiaosenho.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.model.dto.*;
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

    /**
     * 添加课程基本信息
     * @param companyId
     * @param addCourseBaseInfoDto
     * @return
     */
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseBaseInfoDto addCourseBaseInfoDto);

    /**
     * 根据课程id获取基本信息和营销信息
     * @param courseId
     * @return
     */
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    /**
     * 修改课程基本信息
     * @param companyId
     * @param editCourseBaseInfoDto
     * @return
     */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseBaseInfoDto editCourseBaseInfoDto);

    /**
     * 根据课程id删除课程
     * @param courseId
     */
    public void deleteCourseById(Long courseId);
}
