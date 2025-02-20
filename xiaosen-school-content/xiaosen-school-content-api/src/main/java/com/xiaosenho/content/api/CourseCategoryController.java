package com.xiaosenho.content.api;

import com.xiaosenho.base.constant.ContentConstant;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-15 14:08
 * @Description:
 */
@RestController
@Api(tags = "课程分类管理接口")
public class CourseCategoryController {
    @Resource
    CourseCategoryService courseCategoryService;
    @GetMapping("/course-category/tree-nodes")
    @ApiOperation("课程分类查询")
    public List<CourseCategoryTreeDto> treeNodes(){
        return courseCategoryService.queryCourseCategoryList(ContentConstant.CATEGORY_ROOT_ID);
    }
}
