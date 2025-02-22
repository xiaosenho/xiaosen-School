package com.xiaosenho.content.api;

import com.xiaosenho.content.model.dto.CoursePreviewDto;
import com.xiaosenho.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

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
     * 课程预览接口
     *
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
        //TODO 登录功能，机构id获取
        long companyId = 1232141425L;
        coursePublishService.commitAudit(companyId,courseId);
    }

    @ApiOperation("课程发布")
    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void coursepublish(@PathVariable("courseId") Long courseId) {
        //TODO 登录功能，机构id获取
        long companyId = 1232141425L;
        coursePublishService.publish(companyId,courseId);
    }
}
