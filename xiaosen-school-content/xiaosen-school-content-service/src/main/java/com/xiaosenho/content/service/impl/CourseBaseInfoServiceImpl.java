package com.xiaosenho.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.mapper.CourseBaseMapper;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.model.po.CourseCategory;
import com.xiaosenho.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-15 11:00
 * @Description:
 */
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper,CourseBase> implements CourseBaseInfoService {
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        LambdaQueryWrapper<CourseBase> lambdaQueryWrapper = new QueryWrapper<CourseBase>().lambda();
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus())
                .like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),CourseBase::getName,queryCourseParamsDto.getCourseName())
                .eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),CourseBase::getStatus,queryCourseParamsDto.getPublishStatus());
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        IPage<CourseBase> courseBaseIPage = page(page, lambdaQueryWrapper);

        List<CourseBase> items = courseBaseIPage.getRecords();
        long counts = courseBaseIPage.getTotal();
        long size = courseBaseIPage.getSize();
        long pages = courseBaseIPage.getCurrent();
        return new PageResult<CourseBase>(items,counts,pages,size);
    }
}
