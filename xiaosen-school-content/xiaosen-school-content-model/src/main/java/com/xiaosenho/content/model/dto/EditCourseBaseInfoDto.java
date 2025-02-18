package com.xiaosenho.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: 作者
 * @create: 2025-02-17 14:16
 * @Description:
 */
@Data
@ApiModel(value="EditCourseDto", description="修改课程基本信息")
public class EditCourseBaseInfoDto extends AddCourseBaseInfoDto {

    @ApiModelProperty(value = "课程id", required = true)
    private Long id;

}
