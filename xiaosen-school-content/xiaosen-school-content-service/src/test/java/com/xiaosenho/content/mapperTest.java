package com.xiaosenho.content;

import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.mapper.CourseBaseMapper;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author: 作者
 * @create: 2025-02-13 21:12
 * @Description:
 */
@SpringBootTest
public class mapperTest {
    @Resource
    private CourseBaseInfoService courseBaseInfoService;
    @Test
    public void test(){
        PageParams pageParams = new PageParams(1L, 10L);
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        System.out.println(courseBasePageResult);
    }
}
