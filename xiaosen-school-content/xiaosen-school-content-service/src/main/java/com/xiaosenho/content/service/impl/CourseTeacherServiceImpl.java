package com.xiaosenho.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.content.mapper.CourseTeacherMapper;
import com.xiaosenho.content.model.po.CourseTeacher;
import com.xiaosenho.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {

    @Override
    public List<CourseTeacher> getListByCourseId(Long courseId) {
        return list(new LambdaQueryWrapper<CourseTeacher>().eq(CourseTeacher::getCourseId, courseId));
    }

    @Transactional
    @Override
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher) {
        boolean save = saveOrUpdate(courseTeacher);
        if (!save) {
            ServiceException.cast("添加课程教师失败");
        }
        return courseTeacher;
    }

    @Override
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher) {
        boolean update = updateById(courseTeacher);
        if (!update) {
            ServiceException.cast("更新课程教师失败");
        }
        return courseTeacher;
    }

    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        boolean remove = remove(new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, courseId)
                .eq(CourseTeacher::getId, teacherId));
        if (!remove) {
            ServiceException.cast("删除课程教师失败");
        }
    }
}
