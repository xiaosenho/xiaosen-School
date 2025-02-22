package com.xiaosenho.content;

import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.content.mapper.CourseBaseMapper;
import com.xiaosenho.content.mapper.TeachplanMapper;
import com.xiaosenho.content.model.dto.QueryCourseParamsDto;
import com.xiaosenho.content.model.dto.TeachPlanDto;
import com.xiaosenho.content.model.po.CourseBase;
import com.xiaosenho.content.service.CourseBaseInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-13 21:12
 * @Description:
 */
@SpringBootTest
public class mapperTest {
    @Resource
    private CourseBaseInfoService courseBaseInfoService;
    @Resource
    private TeachplanMapper teachplanMapper;
    @Test
    public void test(){
        PageParams pageParams = new PageParams(1L, 10L);
        QueryCourseParamsDto queryCourseParamsDto = new QueryCourseParamsDto();
        queryCourseParamsDto.setCourseName("java");
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParamsDto);
        System.out.println(courseBasePageResult);
    }

    @Test
    public void test2(){
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachPlanDtos);
    }

    public List<List<Integer>> fourSum(int[] nums, int target) {
        Arrays.sort(nums);
        int size = nums.length;
        List<List<Integer>> result = new ArrayList();
        for(int i = 0; i < size-3; i++){
            if(i>0&&nums[i]==nums[i-1])continue;
            for(int j = i+1; j < size-2; j++){
                if(j>i+1&&nums[j]==nums[j-1])continue;
                int sum = nums[i]+nums[j];
                int a = j+1;
                int b = size-1;
                while(a<b){
                    sum += nums[a]+nums[b];
                    if(sum>target){//右指针左移
                        while(a<b && nums[b]==nums[--b]);
                    }else if(sum<target){//左指针右移
                        while(a<b && nums[a]==nums[++a]);
                    }else{
                        result.add(Arrays.asList(nums[i],nums[j],nums[a],nums[b]));
                        while(a<b && nums[b]==nums[--b]);
                        while(a<b && nums[a]==nums[++a]);
                    }
                }
            }
        }
        return result;
    }
    @Test
    public void test3(){
        System.out.println(fourSum(new int[]{2,2,2,2,2},8));
    }
}
