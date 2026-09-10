package com.usst.thumbs.service.ServiceImpl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.usst.thumbs.common.exception.BusinessException;
import com.usst.thumbs.model.User;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.FileService;
import com.usst.thumbs.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final COSClient cosClient;
    private final UserService userService;

    private Set<String> allowedExtensions = Set.of(
      "jpg","jpeg","png","webp","gif"
    );

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    @Value("${tencent.cos.bucket}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile file, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ResultType.NOT_LOGIN,"用户未登录");
        }
        if(file == null || file.isEmpty())
            throw new BusinessException(ResultType.PARAM_ERROR,"上传内容为空");
        // extName 返回值本身不带点，例如 "jpg"。浏览器上传的文件名可能缺失后缀，
        // 所以同时兼容已规范化的文件后缀和 Multipart 的 MIME 类型。
        String extension = FileUtil.extName(file.getOriginalFilename());
        String normalizedExtension = extension == null
                ? ""
                : extension.trim().toLowerCase(Locale.ROOT);
        String contentType = file.getContentType();
        String normalizedContentType = contentType == null
                ? ""
                : contentType.toLowerCase(Locale.ROOT).split(";", 2)[0].trim();
        if (!allowedExtensions.contains(normalizedExtension)
                && !ALLOWED_IMAGE_TYPES.contains(normalizedContentType)) {
            throw new BusinessException(ResultType.PARAM_ERROR,"不支持的图片类型");
        }
        long maxSize = 5*1024*1024;
        if(file.getSize()>maxSize)
            throw new BusinessException(ResultType.PARAM_ERROR,"图片尺寸大小不超过5M");
        String rootDir = System.getProperty("user.dir") + File.separator + "/uploads";
        String prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + UUID.randomUUID().toString().replace("-","");
        String fileName = prefix + ".jpg";
        String filePath = rootDir + File.separator + fileName;
        String compressedName = prefix + "_compressed.jpg";
        String compressedPath = rootDir + File.separator + compressedName;
        try {
            FileUtil.mkdir(rootDir);
            file.transferTo(new File(filePath));
            ImgUtil.compress(
                    new File(filePath),
                    new File(compressedPath),
                    0.5f
            );
            String key = "blog/" + compressedName;
            cosClient.putObject(bucketName, key, new File(compressedPath));
            return cosClient.getObjectUrl(bucketName, key).toString();
        } catch (IOException e) {
            throw new BusinessException(ResultType.SYSTEM_ERROR,"文件上传失败");
        }finally {
            FileUtil.del(new File(filePath));
            FileUtil.del(new File(compressedPath));
        }
    }


    @Override
    public String downloadRemoteImage(String imageUrl) {
        checkImageUrl(imageUrl);

        try {
            URL url = new URL(imageUrl).toURI().toURL();
            URLConnection connection = url.openConnection();

            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36");

            String contentType = connection.getContentType();

            if (contentType == null) {
                throw new BusinessException(ResultType.PARAM_ERROR, "请求参数错误");
            }

            String filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-"
                    + UUID.randomUUID().toString().replace("-", "").substring(0, 8)
                    + ".jpg";

            Path coverDir = Paths.get(uploadDir, "covers");
            Files.createDirectories(coverDir);

            Path targetPath = coverDir.resolve(filename);

            try (InputStream inputStream = connection.getInputStream()) {
                Thumbnails.of(inputStream)
                        .size(600,600)
                        .outputFormat("jpg")
                        .outputQuality(0.75)
                        .toFile(targetPath.toFile());
            }

            return urlPrefix + "/covers/" + filename;
        } catch (IOException | URISyntaxException e) {
            throw new BusinessException(ResultType.SYSTEM_ERROR, "远程图片下载失败");
        }
    }

    private void checkImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new BusinessException(ResultType.PARAM_ERROR, "图片地址不能为空");
        }
    }
    String getSuffixByContentType(String contentType){
        if(contentType.contains("/jpg") || contentType.contains("/jpeg"))
            return ".jpg";
        if (contentType.contains("png")) {
            return ".png";
        }
        if (contentType.contains("webp")) {
            return ".webp";
        }
        return ".jpg";
    }
}
