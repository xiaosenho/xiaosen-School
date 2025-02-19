package com.xiaosenho.media.service.impl;

import com.alibaba.nacos.api.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xiaosenho.base.constant.CourseAuditStatusEnum;
import com.xiaosenho.base.constant.MediaTypeEnum;
import com.xiaosenho.base.constant.ResourceStatusEnum;
import com.xiaosenho.base.exception.ServiceException;
import com.xiaosenho.base.model.PageParams;
import com.xiaosenho.base.model.PageResult;
import com.xiaosenho.base.model.RestResponse;
import com.xiaosenho.media.mapper.MediaFilesMapper;
import com.xiaosenho.media.model.dto.QueryMediaParamsDto;
import com.xiaosenho.media.model.dto.UploadFileParamsDto;
import com.xiaosenho.media.model.dto.UploadFileResultDto;
import com.xiaosenho.media.model.po.MediaFiles;
import com.xiaosenho.media.service.MediaFileService;
import com.xiaosenho.media.utils.MinioUtil;
import io.minio.ObjectWriteResponse;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Service
@Slf4j
public class MediaFileServiceImpl extends ServiceImpl<MediaFilesMapper,MediaFiles> implements MediaFileService {

    @Resource
    private MediaFilesMapper mediaFilesMapper;
    @Resource
    private MinioUtil minioUtil;
    //自己注入自己，使用自己的代理对象，保证事务不失效
    @Resource
    private MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String filesBucketName;//普通文件存储桶名

    @Value("${minio.bucket.videofiles}")
    private String videoFilesBucketName;//视频文件存储桶名

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String filepath) {
        //把文件上传到minio
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf(".") + 1);//文件后缀
        //使用工具类根据扩展名取出mimeType
        String mimeType = getMimeType(extension);
        //存储文件名：由（年/月/日/文件md5码）组成
        String defaultFolderPath = getDefaultFolderPath();
        File file = new File(filepath);
        if (file == null) {
            ServiceException.cast("文件不存在");
        }
        String fileMd5 = getFileMd5(file);
        String objectName = defaultFolderPath + fileMd5;

        //查询数据库中是否存在相同md5的文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {//如果已上传直接返回原文件信息
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
            return uploadFileResultDto;
        }
        //通过minio工具上传文件
        //由于存在网络IO，不能对整个方法使用@Transactional注解，否则长时间占用数据库连接
        boolean upload = minioUtil.upload(filesBucketName, objectName, filepath, mimeType);
        if (!upload) {
            ServiceException.cast("上传文件失败");
        }
        //保存文件信息到数据库
        //使用代理对象，保证事务不失效
        MediaFiles mediaFiles1 = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, filesBucketName, objectName);
        //返回文件信息
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles1, uploadFileResultDto);
        return uploadFileResultDto;
    }

    //根据文件扩展名获取mimeType（内容类型）
    public String getMimeType(String extension) {
        if (StringUtils.isEmpty(extension)) {
            ServiceException.cast("文件格式错误");
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    @Transactional
    public MediaFiles addMediaFilesToDb(
            Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto,
            String bucket, String objectName) {
        MediaFiles mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        mediaFiles.setId(fileMd5);
        mediaFiles.setFileId(fileMd5);
        mediaFiles.setUrl("/" + bucket + "/" + objectName);
        mediaFiles.setCompanyId(companyId);
        mediaFiles.setFilePath(objectName);
        mediaFiles.setBucket(bucket);
        mediaFiles.setCreateDate(LocalDateTime.now());
        mediaFiles.setAuditStatus(CourseAuditStatusEnum.SUBMITTED.getCode());//已提交
        mediaFiles.setStatus(ResourceStatusEnum.ACTIVE.getCode());//资源已启用
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert < 0) {
            log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
            ServiceException.cast("保存文件信息到数据库失败");
        }
        log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        //检查数据库中是否存在
        //查询数据库中是否存在相同md5的文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {//数据库中存在,判断minio中是否存在
            //检查minio中是否存在
            String filePath = mediaFiles.getFilePath();
            StatObjectResponse response = minioUtil.exist(videoFilesBucketName, filePath);
            if (response!=null){
                return RestResponse.success(false);//文件已存在
            }
        }
        return RestResponse.success(true);//文件不存在
    }

    @Override
    public RestResponse<Boolean> checkchunk(String fileMd5, int chunk) {
        String chunkFileFolder =  getChunkFileFolder(fileMd5);
        String chunkPath = chunkFileFolder + chunk; //分块文件路径
        StatObjectResponse response = minioUtil.exist(videoFilesBucketName, chunkPath);
        if (response!=null){
            return RestResponse.success(true);//文件已存在
        }
        return RestResponse.success(false);//分块文件不存在
    }

    @Override
    public RestResponse uploadchunk(String localFilePath, String fileMd5, int chunk) {
        //获取分块文件目录，由md5码前两位决定
        String chunkFileFolder =  getChunkFileFolder(fileMd5);
        //上传分块文件
        String chunkPath = chunkFileFolder + chunk; //分块文件路径
        boolean upload = minioUtil.upload(videoFilesBucketName, chunkPath, localFilePath, null);
        if(!upload)return RestResponse.validfail(false,"分块文件上传失败");
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, String fileName, int chunkTotal) {
        //获取分块文件目录，由md5码前两位决定
        String chunkFileFolder =  getChunkFileFolder(fileMd5);
        //保存文件信息到数据库
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        //存储文件名：由（年/月/日/文件md5码）组成
        String defaultFolderPath = getDefaultFolderPath();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);//文件后缀
        String objectName = defaultFolderPath + fileMd5 + "." + extension;//文件名
        try {
            //合并分块文件
            boolean merge = minioUtil.merge(videoFilesBucketName, chunkFileFolder, objectName, chunkTotal);
            if(!merge){
                return RestResponse.validfail(false,"分块文件合并失败");
            }
            //获取合并后的文件信息
    //        File minioFile = minioUtil.getFile(videoFilesBucketName, objectName);
    //        if (minioFile==null){
    //            return RestResponse.validfail(false,"分块文件合并失败");
    //        }
//            //校验文件
//            try (InputStream newFileInputStream = Files.newInputStream(minioFile.toPath())) {
//                //minio上文件的md5值
//                String md5Hex = DigestUtils.md5Hex(newFileInputStream);
//                //比较md5值，不一致则说明文件不完整
//                if(!fileMd5.equals(md5Hex)){
//                    return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
//                }
//                //文件大小
//                uploadFileParamsDto.setFileSize(minioFile.length());
//            }catch (Exception e){
//                log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
//                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
//            }
            //重新获取文件流计算md5值过于耗时，且消耗资源，这里注释掉了
            //由于minio 分片合并后，etag和源文件的md5码值不同，这里不进行校验，知识判断文件是否已经存在
            StatObjectResponse response = minioUtil.exist(videoFilesBucketName, objectName);
            if(response==null){
                return RestResponse.validfail(false,"分块文件合并失败");
            }
            uploadFileParamsDto.setFilename(fileName);
            uploadFileParamsDto.setFileSize(response.size());
            uploadFileParamsDto.setFileType(MediaTypeEnum.VIDEO.getCode());//视频格式
            currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, videoFilesBucketName, objectName);
            return RestResponse.success(true);
        } catch (Exception e) {
            return RestResponse.validfail(false,"分块文件合并失败");
        } finally {
            //删除分块文件
            minioUtil.removeChunk(videoFilesBucketName, chunkFileFolder, chunkTotal);
        }
    }

    //获取分块文件目录，由md5码前两位决定
    private String getChunkFileFolder(String fileMd5) {
        String floder1 = String.valueOf(fileMd5.charAt(0));
        String floder2 = String.valueOf(fileMd5.charAt(1));
        return floder1 + "/" + floder2 + "/" ;
    }
}
