package com.xiaosenho.content.model.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-21 11:37
 * @Description:
 */
@Data
public class CoursePreviewDto {

    //课程基本信息,课程营销信息
    CourseBaseInfoDto courseBase;


    //课程计划信息
    List<TeachPlanDto> teachplans;

    //师资信息暂时不加...

}
