package com.wujie.im.util;

import lombok.Data;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * FTP工具类
 */
@Data
@Component
public class FtpTool {

    private static final Logger log = LoggerFactory.getLogger(FtpTool.class);
    private static final int TIMEOUT = 1000 * 30;

    @Value("${ftp.host}")
    private String HOST;

    @Value("${ftp.username}")
    private String USER_NAME;

    @Value("${ftp.password}")
    private String PWD;

    @Value("${ftp.port}")
    private Integer PORT;

    @Value("${ftp.path}")
    private String PATH;

    @Value("${ftp.url-prefix:http://ftp.pnkx.top:8}")
    private String urlPrefix;

    public FTPClient connectFtp() {
        FTPClient ftpClient;
        try {
            ftpClient = new FTPClient();
            ftpClient.setConnectTimeout(TIMEOUT);
            ftpClient.connect(HOST, PORT);
            ftpClient.setRemoteVerificationEnabled(false);
            ftpClient.login(USER_NAME, PWD);
            if (FTPReply.isPositiveCompletion(ftpClient.sendCommand("OPTS UTF8", "ON"))) {
                ftpClient.setControlEncoding("UTF-8");
            } else {
                ftpClient.setControlEncoding("ISO-8859-1");
            }
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                log.error("连接FTP失败，用户名或密码错误");
                ftpClient.disconnect();
            } else {
                log.info("FTP连接成功!");
            }
        } catch (Exception e) {
            log.error("登录FTP失败，请检查FTP配置：{}", e.toString());
            return null;
        }
        return ftpClient;
    }

    public void closeFtpClient(FTPClient ftpClient) {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error("关闭FTP连接异常：{}", e.toString());
            }
        }
    }

    public String uploadMultipartFile(MultipartFile file, String dir) {
        String dateDir = LocalDate.now().toString().replace("-", "/");
        String fullDir = PATH + dir + "/" + dateDir;
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        FTPClient ftpClient = connectFtp();
        if (ftpClient == null) return null;
        try {
            createDirectory(ftpClient, fullDir);
            ftpClient.changeWorkingDirectory(fullDir);
            try (InputStream input = file.getInputStream()) {
                String saved = uploadFile(ftpClient, fullDir, fileName, input);
                return saved != null ? urlPrefix + "/" + dir + "/" + dateDir + "/" + saved : null;
            }
        } catch (IOException e) {
            log.error("FTP上传失败：{}", e.toString());
            return null;
        } finally {
            closeFtpClient(ftpClient);
        }
    }

    private String uploadFile(FTPClient ftpClient, String serviceDec, String fileName, InputStream inputStream) {
        try {
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.makeDirectory(serviceDec);
            ftpClient.changeWorkingDirectory(serviceDec);
            ftpClient.storeFile(new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1), inputStream);
            inputStream.close();
            ftpClient.logout();
            log.info("FTP上传成功：{}/{}", serviceDec, fileName);
            return fileName;
        } catch (Exception e) {
            log.error("FTP上传失败：{}", e.toString());
            return null;
        } finally {
            closeFtpClient(ftpClient);
        }
    }

    private boolean changeWorkingDirectory(FTPClient ftpClient, String directory) {
        try {
            return ftpClient.changeWorkingDirectory(directory);
        } catch (IOException e) {
            return false;
        }
    }

    private boolean createDirectory(FTPClient ftpClient, String remote) throws IOException {
        boolean success = true;
        String directory = remote + "/";
        if (!directory.equalsIgnoreCase("/") && !changeWorkingDirectory(ftpClient, directory)) {
            int start = directory.startsWith("/") ? 1 : 0;
            int end = directory.indexOf("/", start);
            String paths = "";
            while (true) {
                String subDir = new String(remote.substring(start, end).getBytes("GBK"), StandardCharsets.ISO_8859_1);
                String path = paths + "/" + subDir;
                if (!existFile(ftpClient, path)) {
                    ftpClient.makeDirectory(subDir);
                    changeWorkingDirectory(ftpClient, subDir);
                } else {
                    changeWorkingDirectory(ftpClient, subDir);
                }
                paths = paths + "/" + subDir;
                start = end + 1;
                end = directory.indexOf("/", start);
                if (end <= start) break;
            }
        }
        return success;
    }

    private boolean existFile(FTPClient ftpClient, String path) throws IOException {
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        return ftpFileArr.length > 0;
    }
}
