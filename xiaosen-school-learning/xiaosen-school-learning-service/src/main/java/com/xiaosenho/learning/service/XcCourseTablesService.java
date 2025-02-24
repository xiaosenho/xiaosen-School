package com.xiaosenho.learning.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.learning.model.dto.XcChooseCourseDto;
import com.xiaosenho.learning.model.dto.XcCourseTablesDto;
import com.xiaosenho.learning.model.po.XcCourseTables;

public interface XcCourseTablesService extends IService<XcCourseTables> {
    /**
     * 添加选课
     * @param userId
     * @param courseId
     * @return
     */
    XcChooseCourseDto addCourse(String userId, Long courseId);

    /**
     * 获取课程状态，判断学习资格，
     * @param userId
     * @param courseId
     * @return
     */
    XcCourseTablesDto getLearnstatus(String userId, Long courseId);
}
