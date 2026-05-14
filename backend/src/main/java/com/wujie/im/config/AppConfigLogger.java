package com.wujie.im.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
public class AppConfigLogger {

    @Value("${spring.datasource.url:未配置}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:未配置}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Value("${spring.data.redis.host:未配置}")
    private String redisHost;

    @Value("${spring.data.redis.port:未配置}")
    private String redisPort;

    @Value("${spring.data.redis.database:未配置}")
    private String redisDatabase;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${wujie.im.crypto.aes-key:}")
    private String aesKey;

    @Value("${ftp.host:未配置}")
    private String ftpHost;

    @Value("${ftp.port:未配置}")
    private String ftpPort;

    @Value("${ftp.username:未配置}")
    private String ftpUsername;

    @Value("${ftp.password:}")
    private String ftpPassword;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void printConfig() {
        log.info("========== 应用配置信息 ==========");
        log.info("数据源 URL: {}", datasourceUrl);
        log.info("数据源用户名: {}", datasourceUsername);
        log.info("数据源密码: {}", maskPassword(datasourcePassword));
        log.info("Redis 主机: {}", redisHost);
        log.info("Redis 端口: {}", redisPort);
        log.info("Redis 数据库: {}", redisDatabase);
        log.info("JWT 密钥: {}", maskPassword(jwtSecret));
        log.info("AES 密钥: {}", maskPassword(aesKey));
        log.info("FTP 主机: {}", ftpHost);
        log.info("FTP 端口: {}", ftpPort);
        log.info("FTP 用户名: {}", ftpUsername);
        log.info("FTP 密码: {}", maskPassword(ftpPassword));

        // 打印从环境变量读取的原始值（调试用）
        log.info("--- 环境变量原始值 ---");
        logEnvVar("MYSQL_URL");
        logEnvVar("MYSQL_USERNAME");
        logEnvVar("MYSQL_PASSWORD");
        logEnvVar("REDIS_HOST");
        logEnvVar("REDIS_PORT");
        logEnvVar("JWT_SECRET");
        logEnvVar("AES_KEY");
        logEnvVar("FTP_HOST");
        logEnvVar("FTP_PORT");
        logEnvVar("FTP_USERNAME");
        logEnvVar("FTP_PASSWORD");
        log.info("========== 配置信息结束 ==========");
    }

    private void logEnvVar(String key) {
        String value = environment.getProperty(key);
        if (value != null) {
            if (key.toLowerCase().contains("password") || key.toLowerCase().contains("secret") || key.toLowerCase().contains("key")) {
                log.info("{}: {}", key, maskPassword(value));
            } else {
                log.info("{}: {}", key, value);
            }
        } else {
            log.info("{}: 未设置（使用默认值）", key);
        }
    }

    private String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "<空>";
        }
        if (password.length() <= 2) {
            return "*";
        }
        return password.charAt(0) + "***" + password.charAt(password.length() - 1);
    }
}
