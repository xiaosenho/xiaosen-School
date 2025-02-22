import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: 作者
 * @create: 2025-02-18 14:58
 * @Description:
 */
public class MinioTest {
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://106.54.193.108:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void test() {
        //使用工具类根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".png");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder()
                    .bucket("testbucket")
//                    .object("test001.mp4")
                    .object("001/test001.png")//添加子目录
                    .filename("C:\\Users\\hello\\OneDrive\\图片\\bev镜像.png")
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("上传失败");
        }
    }

    @Test
    public void delete(){
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder().bucket("testbucket").object("001/test001.png").build());
            System.out.println("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("删除失败");
        }
    }

    //查询文件
    @Test
    public void getFile() throws IOException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("001/test001.png").build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\upload\\1_2.png"));
        ) {
            IOUtils.copy(inputStream,outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //校验文件的完整性对文件的内容进行md5
        FileInputStream fileInputStream1 = new FileInputStream(new File("D:\\develop\\upload\\1.mp4"));
        String source_md5 = DigestUtils.md5Hex(fileInputStream1);
        FileInputStream fileInputStream = new FileInputStream(new File("D:\\develop\\upload\\1a.mp4"));
        String local_md5 = DigestUtils.md5Hex(fileInputStream);
        if(source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }

    }

    //文件分片
    @Test
    public void sliceFile() throws Exception {
        File file = new File("D:\\upload\\output.mp4");
        String chunkPath = "D:\\upload\\chunk\\";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }
        long size = file.length();
        long partSize = 5 * 1024 * 1024;//分片大小5M
        int partCount = (int) Math.ceil((double) size / partSize);//向上取整，分片个数
        RandomAccessFile raf_read = new RandomAccessFile(file, "r");
        byte[] bytes = new byte[1024];//缓存区
        for (int i = 0; i < partCount; i++) {
            File partFile = new File(chunkPath+i);//分块文件
            if(partFile.exists()){
              partFile.delete();
            }
            boolean newFile = partFile.createNewFile();
            if (newFile) {
                //向分块文件中写数据
                RandomAccessFile raf_write = new RandomAccessFile(partFile, "rw");
                int len = -1;
                while ((len = raf_read.read(bytes)) != -1) {
                    raf_write.write(bytes, 0, len);
                    if (partFile.length() >= partSize) {
                        break;
                    }
                }
                raf_write.close();
                System.out.println("完成分块"+i);
            }
        }
        raf_read.close();
    }


    //上传分块文件到minio
    @Test
    public void uploadChunkFile() throws Exception {
        for (int i = 0; i < 104; i++) {
            try {
                UploadObjectArgs testbucket = UploadObjectArgs.builder()
                        .bucket("video")
//                    .object("test001.mp4")
                        .object("test/chunk/"+i)//添加子目录
                        .filename("D:\\upload\\chunk\\"+i)
//                        .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                        .build();
                minioClient.uploadObject(testbucket);
                System.out.println("上传成功");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("上传失败");
            }
        }
    }

    //合并文件
    @Test
    public void mergeFile() throws Exception {
        List<ComposeSource> composeSources = new ArrayList<>();
        for (int i = 0; i < 104; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                .bucket("video")
                .object("test/chunk/"+i)
                .build();
            composeSources.add(composeSource);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("video")
                .object("test/1.mp4")
                .sources(composeSources)//源文件参数
                .build();
        minioClient.composeObject(composeObjectArgs);
    }


}
