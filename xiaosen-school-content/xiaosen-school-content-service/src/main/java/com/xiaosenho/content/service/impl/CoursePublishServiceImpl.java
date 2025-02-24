package com.xiaosenho.content.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xiaosenho.base.constant.CourseAuditStatusEnum;
import com.xiaosenho.base.constant.CoursePublishStatusEnum;
import com.xiaosenho.base.exception.CommonError;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.content.config.MultipartSupportConfig;
import com.xiaosenho.content.feignclient.MediaServiceClient;
import com.xiaosenho.content.mapper.*;
import com.xiaosenho.content.model.dto.CourseBaseInfoDto;
import com.xiaosenho.content.model.dto.CoursePreviewDto;
import com.xiaosenho.content.model.dto.TeachPlanDto;
import com.xiaosenho.content.model.po.*;
import com.xiaosenho.content.service.CoursePublishService;
import com.xiaosenho.content.service.TeachplanService;
import com.xiaosenho.messagesdk.model.po.MqMessage;
import com.xiaosenho.messagesdk.service.MqMessageService;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {
    @Resource
    private CourseBaseMapper courseBaseMapper;
    @Resource
    private CourseMarketMapper courseMarketMapper;
    @Resource
    private TeachplanService teachplanService;
    @Resource
    private CourseCategoryMapper courseCategoryMapper;
    @Resource
    private CoursePublishPreMapper coursePublishPreMapper;
    @Resource
    private MqMessageService mqMessageService;
    @Resource
    private MediaServiceClient mediaServiceClient;


    /**
     * 预览课程
     * @param courseId
     * @return
     */
    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        // 根据课程id查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            log.error("课程不存在，课程id:{}",courseId);
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        // 根据课程id查询课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }
        // 根据课程id查询课程计划信息
        List<TeachPlanDto> teachPlanDtos = teachplanService.getTeachPlanTreeNodesById(courseId);
        // 封装课程预览信息
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();
        coursePreviewDto.setCourseBase(courseBaseInfoDto);
        coursePreviewDto.setTeachplans(teachPlanDtos);
        return coursePreviewDto;
    }

    /**
     * 提交审核
     * @param courseId
     */
    @Transactional
    @Override
    public void commitAudit(Long companyId,Long courseId) {
        // 查询课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            ServiceException.cast("课程不存在");
        }
        // 非本机构id的课程不能操作
        if(!Objects.equals(companyId,courseBase.getCompanyId())){
            ServiceException.cast("本机构不能操作非本机构课程");
        }
        // 已提交，待审核状态不能重复提交
        if(CourseAuditStatusEnum.SUBMITTED.getCode().equals(courseBase.getAuditStatus())){
            ServiceException.cast("课程已提交，请勿重复提交");
        }
        // 没有上传封面不能提交
        if(StringUtils.isEmpty(courseBase.getPic())){
            ServiceException.cast("课程封面不能为空");
        }
        // 没有课程计划不能提交
        List<TeachPlanDto> teachPlanTreeNodes = teachplanService.getTeachPlanTreeNodesById(courseId);
        if(teachPlanTreeNodes == null || teachPlanTreeNodes.isEmpty()){
            ServiceException.cast("课程计划不能为空");
        }

        // 整合课程计划与课程基本信息和课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        Gson gson = new Gson();
        BeanUtils.copyProperties(courseBase,coursePublishPre);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,coursePublishPre);
            String market = gson.toJson(courseMarket);//将营销数据转换为json字符串保存到课程预发布表
            coursePublishPre.setMarket(market);
        }
        String teachPlan = gson.toJson(teachPlanTreeNodes);//将课程计划数据转换为json字符串保存到课程预发布表
        coursePublishPre.setTeachplan(teachPlan);
        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        coursePublishPre.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        coursePublishPre.setMtName(courseCategoryByMt.getName());

        // 向课程预发布表插入数据，如果已存在（即之前审核过了）则更新
        // 更新状态已提交
        coursePublishPre.setStatus(CourseAuditStatusEnum.SUBMITTED.getCode());
        coursePublishPre.setCreateDate(LocalDateTime.now());
        // 查询是否已经存在
        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPreUpdate == null){
            //添加课程预发布记录
            coursePublishPreMapper.insert(coursePublishPre);
        }else{
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程状态为已提交
        courseBase.setAuditStatus(CourseAuditStatusEnum.SUBMITTED.getCode());
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 发布课程
     * @param courseId
     */
    @Transactional
    @Override
    public void publish(Long companyId,Long courseId) {
        // 将课程从课程预发布表查询出来
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            ServiceException.cast("课程不存在");
        }
        if(!Objects.equals(coursePublishPre.getStatus(), CourseAuditStatusEnum.AUDIT_PASSED.getCode())){
            ServiceException.cast("课程未通过审核，不能发布");
        }
        // 非本机构id的课程不能操作
        if(!Objects.equals(companyId,coursePublishPre.getCompanyId())){
            ServiceException.cast("本机构不能操作非本机构课程");
        }
        // 向课程发布表插入或更新数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        // 状态更新为已发布
        coursePublish.setStatus(CoursePublishStatusEnum.PUBLISHED.getCode());
        boolean saveOrUpdate = saveOrUpdate(coursePublish);
        if (!saveOrUpdate){
            ServiceException.cast("课程发布失败");
        }
        // 删除课程预发布表中的数据
        int delete = coursePublishPreMapper.deleteById(courseId);
//        if (delete <= 0){
//            ServiceException.cast("课程发布失败");
//        }
        // 向消息表中插入处理信息
        MqMessage mqMessage = mqMessageService
                .addMessage("course_publish",courseId.toString(),null,null);
        if(mqMessage == null){
            ServiceException.cast(CommonError.UNKOWN_ERROR);
        }

        // 修改课程状态为已发布
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            ServiceException.cast("课程不存在");
        }
        courseBase.setStatus(CoursePublishStatusEnum.PUBLISHED.getCode());
        int update = courseBaseMapper.updateById(courseBase);
    }

    @Override
    public File generateHtml(Long courseId) {
        //静态化文件
        File htmlFile = null;

        try {
            // 获取课程发布数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            if(coursePreviewInfo == null){
                log.debug("课程不存在");
                throw new ServiceException("课程不存在");
            }
            // 封装成model
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());
            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称,获取模板文件
            Template template = configuration.getTemplate("course_template.ftl");

            // 调用FreeMarker生成静态页面
            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.info("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);

        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            ServiceException.cast("课程静态化异常");
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course/" + courseId + ".html");
        if (course == null) {
            ServiceException.cast("上传静态文件异常");
        }
    }
}
