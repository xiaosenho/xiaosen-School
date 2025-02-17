package com.xiaosenho.content.api;

import com.xiaosenho.content.model.dto.SaveTeachplanDto;
import com.xiaosenho.content.model.dto.TeachPlanDto;
import com.xiaosenho.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-17 15:02
 * @Description:
 */
@RestController
@Api(value = "教学计划编辑接口")
@Slf4j
public class TeachPlanController {
    @Resource
    private TeachplanService teachPlanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation(value = "查询教学计划树形结构")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.getTeachPlanTreeNodesById(courseId);
    }

    @PostMapping("/teachplan")
    @ApiOperation(value = "新增或修改教学计划")
    public void addTeachPlan(@RequestBody SaveTeachplanDto addTeachPlanDto){
        teachPlanService.saveTeachPlan(addTeachPlanDto);
    }

}
