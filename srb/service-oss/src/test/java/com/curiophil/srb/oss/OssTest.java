package com.curiophil.srb.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.IOUtils;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;

public class OssTest {

    @Test
    public void testUpload() throws IOException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = "oss-cn-nanjing.aliyuncs.com";
// 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5tR9TMV6z68azwfaN8iw";
        String accessKeySecret = "0FzR9JxVf9ZKPfk8nbW9mg2AoXZhxI";
// 填写Bucket名称，例如examplebucket。
        String bucketName = "curiophil-srb-file";
// 填写文件名。文件名包含路径，不包含Bucket名称。例如exampledir/exampleobject.txt。
        String objectName = "摘录.md";

// 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        byte[] bytes = IOUtils.readStreamAsByteArray(
                new FileInputStream("D:\\workBench\\装b文案.md"));
        String content = "Hello OSS";
        ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));

// 关闭OSSClient。
        ossClient.shutdown();
    }

    @Test
    public void testLocalDateTime() {
        String s = new Gson().toJson(LocalDateTime.now(), LocalDateTime.class);
        System.out.println(s);
    }
}
