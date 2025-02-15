package com.xiaosenho.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaosenho.content.model.dto.CourseCategoryTreeDto;
import com.xiaosenho.content.model.po.CourseCategory;
import com.xiaosenho.content.mapper.CourseCategoryMapper;
import com.xiaosenho.content.service.CourseCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {
    @Resource
    CourseCategoryMapper courseCategoryMapper;
    @Override
    public List<CourseCategoryTreeDto> queryCourseCategoryList(String id) {
        // 调用sql递归获取所有节点信息，这里使用sql语句获取的是有序的列表，保证后面的遍历有序性
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 过滤根节点,将所有节点存储到一个临时map中，便于根据id查找
        Map<String, CourseCategoryTreeDto> treeDtoMaps = courseCategoryTreeDtos.stream()
                .filter(item -> !item.getId().equals(id))
                .collect(Collectors.toMap(CourseCategoryTreeDto::getId, item -> item));

        // 最终返回的list
        List<CourseCategoryTreeDto> resultTreeDtos = new ArrayList<>();

        // 遍历所有节点，将子节点放到父节点的children中
        courseCategoryTreeDtos.stream()
                .filter(item -> !item.getId().equals(id))
                .forEach(item -> {
                    // 如果父节点是根节点，存储List中
                    if(item.getParentid().equals(id)){
                        resultTreeDtos.add(item);
                    }
                    // 如果当前节点有父节点，则将当前节点放到父节点的children中
                    CourseCategoryTreeDto courseCategoryParentDto = treeDtoMaps.get(item.getParentid());
                    // 父节点若为空，表示为其父节点为根节点（已经被过滤），第一级节点已经被添加到resultTreeDtos中
                    // 父节点不为空，则将当前节点添加到父节点的children中
                    if (courseCategoryParentDto != null) {
                        // 初始化children列表
                        if(courseCategoryParentDto.getChildrenTreeNodes() == null){
                            courseCategoryParentDto.setChildrenTreeNodes(new ArrayList<>());
                        }
                        // 添加到父节点的children中
                        courseCategoryParentDto.getChildrenTreeNodes().add(item);
                    }
                });
        return resultTreeDtos;
    }
}
