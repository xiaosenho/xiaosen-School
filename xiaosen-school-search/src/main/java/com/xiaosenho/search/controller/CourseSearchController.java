package com.xiaosenho.search.controller;

import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.search.dto.SearchCourseParamDto;
import com.xiaosenho.search.dto.SearchPageResultDto;
import com.xiaosenho.search.po.CourseIndex;
import com.xiaosenho.search.service.CourseSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description 课程搜索接口
 * @author Mr.M
 * @date 2022/9/24 22:31
 * @version 1.0
 */
@Api(value = "课程搜索接口",tags = "课程搜索接口")
 @RestController
 @RequestMapping("/course")
public class CourseSearchController {

 @Autowired
 CourseSearchService courseSearchService;


 @ApiOperation("课程搜索列表")
  @GetMapping("/list")
 public SearchPageResultDto<CourseIndex> list(PageParams pageParams, SearchCourseParamDto searchCourseParamDto){

    return courseSearchService.queryCoursePubIndex(pageParams,searchCourseParamDto);
   
  }
}
