package com.aimall.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.aimall.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${aimall.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${aimall.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }
        String ext = FileUtil.extName(file.getOriginalFilename());
        String filename = IdUtil.fastSimpleUUID() + "." + ext;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        file.transferTo(new File(dir, filename));
        Map<String, String> result = new HashMap<>();
        result.put("url", urlPrefix + "/" + filename);
        return Result.ok(result);
    }
}
