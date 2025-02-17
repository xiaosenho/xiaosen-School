package com.xiaosenho.content.model.dto;

import com.xiaosenho.content.model.po.Teachplan;
import com.xiaosenho.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-17 14:58
 * @Description:
 */
@Data
@ToString
public class TeachPlanDto extends Teachplan implements Serializable {
    //媒资信息
    private TeachplanMedia teachplanMedia;
    //子章节
    private List<TeachPlanDto> teachPlanTreeNodes;
}
