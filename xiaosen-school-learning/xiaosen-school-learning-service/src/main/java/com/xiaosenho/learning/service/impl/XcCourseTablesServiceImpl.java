package com.xiaosenho.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.constant.ChooseCourseStatusEnum;
import com.xiaosenho.base.constant.ChooseCourseTypeEnum;
import com.xiaosenho.base.constant.ChooseLearningStatusEnum;
import com.xiaosenho.base.constant.CourseChargeEnum;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.content.model.po.CoursePublish;
import com.xiaosenho.learning.feignclient.ContentServiceClient;
import com.xiaosenho.learning.mapper.XcChooseCourseMapper;
import com.xiaosenho.learning.mapper.XcCourseTablesMapper;
import com.xiaosenho.learning.model.dto.XcChooseCourseDto;
import com.xiaosenho.learning.model.dto.XcCourseTablesDto;
import com.xiaosenho.learning.model.po.XcChooseCourse;
import com.xiaosenho.learning.model.po.XcCourseTables;
import com.xiaosenho.learning.service.XcCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-24 16:49
 * @Description:
 */
@Service
@Slf4j
public class XcCourseTablesServiceImpl extends ServiceImpl<XcCourseTablesMapper, XcCourseTables> implements XcCourseTablesService {

    @Resource
    private XcChooseCourseMapper xcChooseCourseMapper;
    @Resource
    private ContentServiceClient contentServiceClient;

    @Override
    public XcChooseCourseDto addCourse(String userId, Long courseId) {
        // 远程调用获取课程发布信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish == null){
            ServiceException.cast("未查询到课程发布信息");
        }
        // 判断课程是否免费，如果免费直接添加到选课记录表和我的课程表中
        String charge = coursepublish.getCharge();
        XcChooseCourseDto xcChooseCourseDto = new XcChooseCourseDto();
        XcChooseCourse xcChooseCourse = null;
        if(CourseChargeEnum.FREE.getCode().equals(charge)){
            xcChooseCourse = addFreeCoruse(userId, coursepublish);//添加到选课记录表
            XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);//添加到我的课程表
            xcChooseCourseDto.setLearnStatus(ChooseLearningStatusEnum.NORMAL_LEARNING.getCode());//返回状态可以正常学习
        }else { //付费课程，先计入选课记录表，等待支付
            xcChooseCourse  = addChargeCoruse(userId, coursepublish);
            xcChooseCourseDto.setLearnStatus(ChooseLearningStatusEnum.NO_ENROLLMENT_OR_PAYMENT.getCode());//返回状态需要支付
        }
        BeanUtils.copyProperties(xcChooseCourse,xcChooseCourseDto);
        return xcChooseCourseDto;
    }

    @Override
    public XcCourseTablesDto getLearnstatus(String userId, Long courseId) {
        // 通过我的课程表获取选课信息
        XcCourseTables xcCourseTables = getXcCourseTables(userId, courseId);
        XcCourseTablesDto xcCourseTablesDto = new XcCourseTablesDto();
        // 判断课程是否为空
        if(xcCourseTables==null){
            // 未选课或未支付
            xcCourseTablesDto.setLearnStatus(ChooseLearningStatusEnum.NO_ENROLLMENT_OR_PAYMENT.getCode());
            return xcCourseTablesDto;
        }
        BeanUtils.copyProperties(xcCourseTables,xcCourseTablesDto);
        // 判断课程是否过期
        if(LocalDateTime.now().isAfter(xcCourseTables.getValidtimeEnd())){
            // 已过期
            xcCourseTablesDto.setLearnStatus(ChooseLearningStatusEnum.EXPIRED_NEED_RENEW_OR_PAY.getCode());
        }else {
            // 正常学习
            xcCourseTablesDto.setLearnStatus(ChooseLearningStatusEnum.NORMAL_LEARNING.getCode());
        }
        return xcCourseTablesDto;
    }

    // 付费课程支付成功，添加到我的课程表中
    @Override
    public boolean addCourseTables(String choosecourseId) {
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(choosecourseId);
        if(xcChooseCourse==null){
            return false;
        }
        // 选课成功
        xcChooseCourse.setStatus(ChooseCourseStatusEnum.ENROLLMENT_SUCCESS.getCode());
        xcChooseCourseMapper.updateById(xcChooseCourse);
        XcCourseTables xcCourseTables = addCourseTabls(xcChooseCourse);
        return true;
    }

    //添加付费课程，加入选课记录表
    public XcChooseCourse addChargeCoruse(String userId,CoursePublish coursepublish){

        //如果存在待支付交易记录直接返回
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, ChooseCourseTypeEnum.PAID_COURSE.getCode())//收费订单
                .eq(XcChooseCourse::getStatus, ChooseCourseStatusEnum.PENDING_PAYMENT.getCode());//待支付
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            return xcChooseCourses.get(0);
        }

        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(coursepublish.getPrice());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType(ChooseCourseTypeEnum.PAID_COURSE.getCode());//收费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus(ChooseCourseStatusEnum.PENDING_PAYMENT.getCode());//待支付

        xcChooseCourse.setValidDays(coursepublish.getValidDays());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        xcChooseCourseMapper.insert(xcChooseCourse);
        return xcChooseCourse;
    }


    //添加免费课程,免费课程加入选课记录表、我的课程表
    public XcChooseCourse addFreeCoruse(String userId, CoursePublish coursepublish) {
        //查询选课记录表是否存在免费的且选课成功的订单
        LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(XcChooseCourse::getUserId, userId)
                .eq(XcChooseCourse::getCourseId, coursepublish.getId())
                .eq(XcChooseCourse::getOrderType, ChooseCourseTypeEnum.FREE_COURSE.getCode())//免费课程
                .eq(XcChooseCourse::getStatus, ChooseCourseStatusEnum.ENROLLMENT_SUCCESS.getCode());//选课成功
        List<XcChooseCourse> xcChooseCourses = xcChooseCourseMapper.selectList(queryWrapper);
        // 选课记录表已经存在免费课程，直接返回
        if (xcChooseCourses != null && xcChooseCourses.size()>0) {
            return xcChooseCourses.get(0);
        }
        //添加选课记录信息
        XcChooseCourse xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setCoursePrice(0f);//免费课程价格为0
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setOrderType(ChooseCourseTypeEnum.FREE_COURSE.getCode());//免费课程
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setStatus(ChooseCourseStatusEnum.ENROLLMENT_SUCCESS.getCode());//选课成功

        xcChooseCourse.setValidDays(365);//免费课程默认365
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        xcChooseCourseMapper.insert(xcChooseCourse);

        return xcChooseCourse;

    }

    /**
     * @description 添加到我的课程表
     * @param xcChooseCourse 选课记录
     * @return com.xiaosenho.learning.model.po.XcCourseTables
     */
    public XcCourseTables addCourseTabls(XcChooseCourse xcChooseCourse){
        //选课记录完成且未过期可以添加课程到课程表
        String status = xcChooseCourse.getStatus();
        if (!ChooseCourseStatusEnum.ENROLLMENT_SUCCESS.getCode().equals(status)){
            ServiceException.cast("选课未成功，无法添加到课程表");
        }
        //查询我的课程表
        XcCourseTables xcCourseTables = getXcCourseTables(xcChooseCourse.getUserId(), xcChooseCourse.getCourseId());
        //如果已存在直接返回
        if(xcCourseTables!=null){
            return xcCourseTables;
        }
        XcCourseTables xcCourseTablesNew = new XcCourseTables();
        xcCourseTablesNew.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTablesNew.setUserId(xcChooseCourse.getUserId());
        xcCourseTablesNew.setCourseId(xcChooseCourse.getCourseId());
        xcCourseTablesNew.setCompanyId(xcChooseCourse.getCompanyId());
        xcCourseTablesNew.setCourseName(xcChooseCourse.getCourseName());
        xcCourseTablesNew.setCreateDate(LocalDateTime.now());
        xcCourseTablesNew.setValidtimeStart(xcChooseCourse.getValidtimeStart());
        xcCourseTablesNew.setValidtimeEnd(xcChooseCourse.getValidtimeEnd());
        xcCourseTablesNew.setCourseType(xcChooseCourse.getOrderType());
        save(xcCourseTablesNew);

        return xcCourseTablesNew;

    }

    /**
     * @description 根据课程和用户查询我的课程表中某一门课程
     * @param userId
     * @param courseId
     * @return com.xiaosenho.learning.model.po.XcCourseTables
     */
    public XcCourseTables getXcCourseTables(String userId,Long courseId){
        XcCourseTables xcCourseTables = getOne(new LambdaQueryWrapper<XcCourseTables>().eq(XcCourseTables::getUserId, userId).eq(XcCourseTables::getCourseId, courseId));
        return xcCourseTables;

    }

}
