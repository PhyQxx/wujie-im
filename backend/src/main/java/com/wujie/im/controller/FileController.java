package com.wujie.im.controller;

import com.wujie.im.common.Result;
import com.wujie.im.util.FtpTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileController {

    private final FtpTool ftpTool;

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;  // 10MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;   // 50MB

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(required = false) Long userId) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        if (file.getSize() > MAX_IMAGE_SIZE) return Result.error("图片大小不能超过10MB");

        String url = ftpTool.uploadMultipartFile(file, "im/images");
        if (url == null) return Result.error("图片上传失败，请稍后再试");

        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("filename", file.getOriginalFilename());
        result.put("size", String.valueOf(file.getSize()));
        return Result.success(result);
    }

    @PostMapping("/file")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(required = false) Long userId) {
        if (file.isEmpty()) return Result.error("文件不能为空");
        if (file.getSize() > MAX_FILE_SIZE) return Result.error("文件大小不能超过50MB");

        String url = ftpTool.uploadMultipartFile(file, "im/files");
        if (url == null) return Result.error("文件上传失败，请稍后再试");

        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        result.put("filename", file.getOriginalFilename());
        result.put("size", String.valueOf(file.getSize()));
        return Result.success(result);
    }
}
