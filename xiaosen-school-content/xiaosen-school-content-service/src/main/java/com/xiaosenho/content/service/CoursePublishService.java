package com.xiaosenho.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaosenho.content.model.dto.CoursePreviewDto;
import com.xiaosenho.content.model.po.CoursePublish;

import java.io.File;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2025-02-13
 */
public interface CoursePublishService extends IService<CoursePublish> {

    CoursePreviewDto getCoursePreviewInfo(Long courseId);

    void commitAudit(Long companyId,Long courseId);

    void publish(Long companyId,Long courseId);
    File generateHtml(Long courseId);
    public void uploadCourseHtml(Long courseId, File file);
}
