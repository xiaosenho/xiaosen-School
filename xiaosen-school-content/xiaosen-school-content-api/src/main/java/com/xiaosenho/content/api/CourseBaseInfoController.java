package com.xiaosenho.content.api;

import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.model.dto.AddCourseBaseInfoDto;
import com.xiaosenho.content.model.dto.CourseBaseInfoDto;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.model.po.CourseCategory;
import com.xiaosenho.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        return courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
    }

    @PostMapping("/course")
    @ApiOperation("新增课程")
    public CourseBaseInfoDto save(@RequestBody AddCourseBaseInfoDto addCourseBaseInfoDto){
        //TODO 登录功能，机构id获取
        long companyId = 1232141425L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseBaseInfoDto);
    }
}
