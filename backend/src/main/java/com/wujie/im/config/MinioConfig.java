package com.wujie.im.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${wujie.im.minio.endpoint:http://127.0.0.1:9010}")
    private String endpoint;

    @Value("${wujie.im.minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${wujie.im.minio.secret-key:minioadmin}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
