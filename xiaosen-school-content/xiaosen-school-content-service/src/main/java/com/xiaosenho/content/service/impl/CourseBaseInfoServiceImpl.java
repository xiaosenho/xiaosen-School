package com.xiaosenho.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.mapper.CourseBaseMapper;
import com.xiaosenho.content.mapper.CourseCategoryMapper;
import com.xiaosenho.content.mapper.CourseMarketMapper;
import com.xiaosenho.content.model.dto.AddCourseBaseInfoDto;
import com.xiaosenho.content.model.dto.CourseBaseInfoDto;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.model.po.CourseCategory;
import com.xiaosenho.content.model.po.CourseMarket;
import com.xiaosenho.content.service.CourseBaseInfoService;
import com.xiaosenho.content.service.CourseCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-15 11:00
 * @Description:
 */
@Service
public class CourseBaseInfoServiceImpl extends ServiceImpl<CourseBaseMapper,CourseBase> implements CourseBaseInfoService {
    @Resource
    private CourseMarketMapper courseMarketMapper;
    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Transactional
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

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseBaseInfoDto addCourseBaseInfoDto) {
        //课程基本表写入信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseBaseInfoDto,courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        //初始审核状态为“未提交”
        courseBase.setAuditStatus("202002");
        //初始发布状态为“未发布”
        courseBase.setStatus("203001");
        boolean saveCourseBase = save(courseBase);
        if(!saveCourseBase){
            throw new RuntimeException("插入课程基本信息表失败");
        }

        //课程营销表写入信息
        CourseMarket courseMarket = new CourseMarket();
        //mbp默认插入主键回显
        courseMarket.setId(courseBase.getId());
        BeanUtils.copyProperties(addCourseBaseInfoDto,courseMarket);
        int save = saveCourseMakrket(courseMarket);
        if(save<=0){
            throw new RuntimeException("插入课程营销表失败");
        }

        return getCourseBaseInfo(courseBase.getId());
    }

    public int saveCourseMakrket(CourseMarket courseMarket){
        //收费规则
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
            }
        }

        //查询营销信息是否已存在
        CourseMarket market = courseMarketMapper.selectById(courseMarket.getId());
        if(market==null){
            return courseMarketMapper.insert(courseMarket);
        }else{
            return courseMarketMapper.updateById(courseMarket);
        }
    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBase courseBase = getById(courseId);
        if(courseBase == null){
            return null;
        }
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){//营销信息不为空
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;

    }
}
