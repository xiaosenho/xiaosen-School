package com.xiaosenho.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.content.mapper.TeachplanMapper;
import com.xiaosenho.content.mapper.TeachplanMediaMapper;
import com.xiaosenho.content.model.dto.SaveTeachplanDto;
import com.xiaosenho.content.model.dto.TeachPlanDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.model.po.Teachplan;
import com.xiaosenho.content.model.po.TeachplanMedia;
import com.xiaosenho.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {
    @Resource
    private TeachplanMapper teachplanMapper;
    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachPlanDto> getTeachPlanTreeNodesById(Long courseId) {
        // 根据课程id查询课程计划，包含了媒资信息
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(courseId);
        // 转换成Map结构，便于查找
        Map<Long, TeachPlanDto> teachPlanDtoMap = teachPlanDtos.stream().collect(Collectors.toMap(TeachPlanDto::getId, a -> a));
        // 结果集
        List<TeachPlanDto> resultTeachPlan = new ArrayList<>();
        teachPlanDtos.forEach(teachPlanDto -> {
            if (teachPlanDto.getParentid() == 0) {//根节点
                resultTeachPlan.add(teachPlanDto);
            }
            TeachPlanDto parentDto = teachPlanDtoMap.get(teachPlanDto.getParentid());
            if (parentDto != null) {//父节点不为空，即非根节点
                if(parentDto.getTeachPlanTreeNodes()==null){
                    parentDto.setTeachPlanTreeNodes(new ArrayList<>());//初始化子节点数组
                }
                parentDto.getTeachPlanTreeNodes().add(teachPlanDto);//添加到子节点数组中
            }
        });
        return resultTeachPlan;
    }

    @Transactional
    @Override
    public void saveTeachPlan(SaveTeachplanDto teachPlanDto) {
        //如果传入的课程计划id为空，则添加，否则修改
        if(teachPlanDto.getId()==null){
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachPlanDto,teachplan);
            //添加默认向后,获取同级课程计划的最大排序号,即前面的元素个数
            if(teachPlanDto.getParentid()==null){
                ServiceException.cast("父节点为空，无法保存");
            }
            int count = countByParent(teachPlanDto.getParentid(),teachPlanDto.getCourseId());
            teachplan.setOrderby(count+1);
            teachplanMapper.insert(teachplan);
        }else{
            //修改
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachPlanDto,teachplan);
            boolean update = updateById(teachplan);
            if(!update){
                ServiceException.cast("修改课程计划失败");
            }
        }
    }
    @Transactional
    @Override
    public void deleteTeachPlan(Long teachplanId) {
        Teachplan teachplan = getById(teachplanId);
        if(teachplan==null){
            ServiceException.cast("课程不存在，无法删除");
        }
        //删除第一级别的大章节时要求大章节下边没有小章节时方可删除
        if(teachplan.getGrade()==1){
            int count = countByParent(teachplan.getId(),teachplan.getCourseId());
            if(count>0){
                ServiceException.cast("课程计划信息还有子级信息，无法操作");
            };
            removeById(teachplanId);
        }
        else if (teachplan.getGrade()==2) {//删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除
            teachplanMediaMapper.delete(new LambdaQueryWrapper<>(TeachplanMedia.class).eq(TeachplanMedia::getTeachplanId, teachplanId));
            removeById(teachplanId);
        }
        //后续节点优先级减一
        List<Teachplan> teachplans = list(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, teachplan.getCourseId())
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .gt(Teachplan::getOrderby,teachplan.getOrderby())//后续节点
        );
        teachplans.forEach(item->{
            item.setOrderby(item.getOrderby()-1);
        });
        updateBatchById(teachplans);
    }

    @Transactional
    @Override
    public void moveDown(Long teachPlanId) {
        //下移需要先获取下一个节点优先级
        Teachplan teachplan = getById(teachPlanId);
        int order = teachplan.getOrderby();
        Teachplan teachplanNext = getOne(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getOrderby, order + 1)
                .eq(Teachplan::getParentid,teachplan.getParentid())
                .eq(Teachplan::getCourseId,teachplan.getCourseId())//用于一级节点判断
        );
        if(teachplanNext!=null){
            teachplan.setOrderby(teachplanNext.getOrderby());
            teachplanNext.setOrderby(order);
            updateById(teachplan);
            updateById(teachplanNext);
        }
    }

    @Transactional
    @Override
    public void moveUp(Long teachPlanId) {
        //上移需要先获取上一个节点优先级
        Teachplan teachplan = getById(teachPlanId);
        int order = teachplan.getOrderby();
        Teachplan teachplanNext = getOne(new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getOrderby, order - 1)
                .eq(Teachplan::getParentid,teachplan.getParentid())
                .eq(Teachplan::getCourseId,teachplan.getCourseId())
        );
        if(teachplanNext!=null){
            teachplan.setOrderby(teachplanNext.getOrderby());
            teachplanNext.setOrderby(order);
            updateById(teachplan);
            updateById(teachplanNext);
        }
    }

    public int countByParent(Long parentid, Long courseId){
        LambdaQueryWrapper<Teachplan> queryWrapper = new QueryWrapper<Teachplan>().lambda();
        queryWrapper.eq(Teachplan::getParentid,parentid).eq(Teachplan::getCourseId,courseId);
        return count(queryWrapper);
    }
}
