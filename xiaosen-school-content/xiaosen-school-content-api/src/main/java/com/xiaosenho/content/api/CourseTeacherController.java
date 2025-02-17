package com.xiaosenho.content.api;

import com.xiaosenho.content.model.po.CourseTeacher;
import com.xiaosenho.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-17 21:32
 * @Description:
 */
@RestController
@Api(tags = "课程教师管理接口")
public class CourseTeacherController {
    @Resource
    private CourseTeacherService courseTeacherService;
    @GetMapping("/courseTeacher/list/{courseId}")
    @ApiOperation("课程教师查询")
    public List<CourseTeacher> getByCourseId(@PathVariable Long courseId) {
        return courseTeacherService.getListByCourseId(courseId);
    }

    @PostMapping("/courseTeacher")
    @ApiOperation("课程教师添加")
    public CourseTeacher save(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.addCourseTeacher(courseTeacher);
    }

    @PutMapping("/courseTeacher")
    @ApiOperation("课程教师修改")
    public CourseTeacher update(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.updateCourseTeacher(courseTeacher);
    }

    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    @ApiOperation("课程教师删除")
    public void removeById(@PathVariable Long courseId, @PathVariable Long teacherId) {
        courseTeacherService.deleteCourseTeacher(courseId, teacherId);
    }
}
