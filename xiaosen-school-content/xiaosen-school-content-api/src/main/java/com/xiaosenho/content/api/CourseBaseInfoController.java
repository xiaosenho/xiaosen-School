package com.xiaosenho.content.api;

import com.xiaosenho.base.exception.ValidationGroups;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.model.dto.*;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.model.po.CourseCategory;
import com.xiaosenho.content.service.CourseBaseInfoService;
import com.xiaosenho.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-13 19:28
 * @Description: 课程信息编辑接口
 */
@RestController
@Api(tags = "课程信息编辑接口")
public class CourseBaseInfoController {
    @Resource
    private CourseBaseInfoService courseBaseInfoService;
    @RequestMapping("/course/list")
    @ApiOperation("课程分页查询")
    @PreAuthorize("hasAuthority('xc_teachmanager_course')") //指定权限，只有有该权限的用户才能访问
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        //机构id获取
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = -1L;//占位符
        if(user != null){ //细粒度授权，用户只能查询自己机构的课程
            companyId = Long.valueOf(user.getCompanyId());
        }
        return courseBaseInfoService.queryCourseBaseList(companyId, pageParams, queryCourseParamsDto);
    }

    @PostMapping("/course")
    @ApiOperation("新增课程")
    public CourseBaseInfoDto save(@RequestBody @Validated(ValidationGroups.Insert.class) AddCourseBaseInfoDto addCourseBaseInfoDto){
        //机构id获取
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(user != null){
            companyId = Long.valueOf(user.getCompanyId());
        }
        return courseBaseInfoService.createCourseBase(companyId,addCourseBaseInfoDto);
    }

    @GetMapping("/course/{courseId}")
    @ApiOperation("根据id查询课程")
    public CourseBaseInfoDto getCourseById(@PathVariable Long courseId){
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(object);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @PutMapping("/course")
    @ApiOperation(value = "修改课程")
    public CourseBaseInfoDto updateCourse(@RequestBody EditCourseBaseInfoDto editCourseBaseInfoDto){
        //机构id获取
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(user != null){
            companyId = Long.valueOf(user.getCompanyId());
        }
        return courseBaseInfoService.updateCourseBase(companyId,editCourseBaseInfoDto);
    }

    @DeleteMapping("/course/{courseId}")
    @ApiOperation("删除课程")
    public void deleteCourse(@PathVariable Long courseId){
        courseBaseInfoService.deleteCourseById(courseId);
    }
}
