package com.xiaosenho.content.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * @author: 作者
 * @create: 2025-02-13 19:19
 * @Description:
 */
@Data
@ToString
@ApiModel(description = "课程查询请求参数")
public class QueryCourseParamsDto {

    //审核状态
    @ApiModelProperty(value = "审核状态")
    private String auditStatus;
    //课程名称
    @ApiModelProperty(value = "课程名称")
    private String courseName;
    //发布状态
    @ApiModelProperty(value = "发布状态")
    private String publishStatus;

}

