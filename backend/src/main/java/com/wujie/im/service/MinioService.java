package com.wujie.im.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${wujie.im.minio.bucket:wujie-im}")
    private String bucketName;

    @Value("${wujie.im.minio.endpoint:http://127.0.0.1:9010}")
    private String endpoint;

    /**
     * 初始化 Bucket
     */
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("MinIO bucket [{}] created", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO bucket init failed", e);
        }
    }

    /**
     * 上传文件
     */
    public String upload(MultipartFile file, String subDir) {
        try {
            initBucket();
            String ext = getExtension(file.getOriginalFilename());
            String objectName = subDir + "/" + UUID.randomUUID() + "." + ext;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return getPresignedUrl(objectName);
        } catch (Exception e) {
            log.error("MinIO upload failed: {}", subDir, e);
            return null;
        }
    }

    /**
     * 获取预签名 URL（默认 7 天有效期）
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Get presigned URL failed: {}", objectName, e);
            return endpoint + "/" + bucketName + "/" + objectName;
        }
    }

    /**
     * 删除文件
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("MinIO delete failed: {}", objectName, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
