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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    // P0 安全修复：文件类型白名单
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${aimall.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${aimall.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return Result.fail(400, "文件不能为空");
        }

        // P0 安全修复：验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.fail(400, "文件大小不能超过 5MB");
        }

        // P0 安全修复：验证文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.fail(400, "无效的文件名");
        }

        String ext = FileUtil.extName(originalFilename);
        if (ext == null || ext.isEmpty()) {
            return Result.fail(400, "文件必须有扩展名");
        }

        ext = ext.toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.fail(400, "只允许上传图片文件（jpg, jpeg, png, gif, webp）");
        }

        // P0 安全修复：验证 MIME 类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail(400, "无效的文件类型");
        }

        // P0 安全修复：验证图片内容（防止伪造扩展名）
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return Result.fail(400, "无效的图片文件");
            }
        } catch (IOException e) {
            return Result.fail(400, "图片文件损坏或格式不支持");
        }

        // 使用 UUID 生成安全的文件名（不使用用户输入）
        String filename = IdUtil.fastSimpleUUID() + "." + ext;
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        file.transferTo(new File(dir, filename));
        Map<String, String> result = new HashMap<>();
        result.put("url", urlPrefix + "/" + filename);
        return Result.ok(result);
    }
}
