package com.xiaosenho.content.api;

import com.alibaba.fastjson.JSON;
import com.xiaosenho.content.model.dto.CourseBaseInfoDto;
import com.xiaosenho.content.model.dto.CoursePreviewDto;
import com.xiaosenho.content.model.dto.TeachPlanDto;
import com.xiaosenho.content.model.po.CoursePublish;
import com.xiaosenho.content.service.CoursePublishService;
import com.xiaosenho.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-21 11:39
 * @Description:
 */
@Controller
@Api(tags = "课程预览发布接口")
public class CoursePublishController {

    @Autowired
    CoursePublishService coursePublishService;


    /**
     * 课程预览接口,根据课程id动态渲染到预览页面中，使用模板引擎生成
     * @param courseId
     * @return
     */
    @ApiOperation("课程预览接口")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") Long courseId) {

        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model", coursePreviewInfo);
        modelAndView.setViewName("course_template");
        return modelAndView;
    }

    /**
     * 课程提交审核接口
     *
     * @param courseId
     */
    @ApiOperation("提交审核")
    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId) {
        //机构id获取
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(user != null){
            companyId = Long.valueOf(user.getCompanyId());
        }
        coursePublishService.commitAudit(companyId,courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        //机构id获取
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(user != null){
            companyId = Long.valueOf(user.getCompanyId());
        }
        coursePublishService.publish(companyId,courseId);
    }

    @ApiOperation("根据课程id查询课程发布信息")
    @ResponseBody
    @GetMapping("coursepublish/{courseId}")
    public CoursePublish getCoursepublish(@PathVariable("courseId") Long courseId){
        return coursePublishService.getById(courseId);
    }

    @ApiOperation("获取课程发布信息")
    @ResponseBody
    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDto getCoursePublish(@PathVariable("courseId") Long courseId) {
        //查询课程发布信息
        CoursePublish coursePublish = coursePublishService.getById(courseId);
        if (coursePublish == null) {
            return new CoursePreviewDto();
        }

        //课程基本信息
        CourseBaseInfoDto courseBase = new CourseBaseInfoDto();
        BeanUtils.copyProperties(coursePublish, courseBase);
        //课程计划
        List<TeachPlanDto> teachplans = JSON.parseArray(coursePublish.getTeachplan(), TeachPlanDto.class);
        CoursePreviewDto coursePreviewInfo = new CoursePreviewDto();
        coursePreviewInfo.setCourseBase(courseBase);
        coursePreviewInfo.setTeachplans(teachplans);
        return coursePreviewInfo;
    }

}
