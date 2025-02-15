package com.xiaosenho.content.model.dto;

import com.xiaosenho.content.model.po.CourseCategory;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-15 14:00
 * @Description:
 */
@Data
@ApiModel("课程分类树形结构")
public class CourseCategoryTreeDto extends CourseCategory implements Serializable {
    @ApiModelProperty("子节点列表")
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
