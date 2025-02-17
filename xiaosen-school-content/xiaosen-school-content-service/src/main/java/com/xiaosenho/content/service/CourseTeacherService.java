package com.xiaosenho.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2025-02-13
 */
public interface CourseTeacherService extends IService<CourseTeacher> {
    /**
     * 根据课程id获取教师列表
     * @param courseId
     * @return
     */
    public List<CourseTeacher> getListByCourseId(Long courseId);

    /**
     * 添加课程教师关系
     * @param courseTeacher
     * @return
     */
    public CourseTeacher addCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 更新课程教师关系
     * @param courseTeacher
     * @return
     */
    public CourseTeacher updateCourseTeacher(CourseTeacher courseTeacher);

    /**
     * 删除课程教师关系
     * @param courseId
     * @param teacherId
     */
    public void deleteCourseTeacher(Long courseId, Long teacherId);
}
