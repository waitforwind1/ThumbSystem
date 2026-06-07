package com.usst.thumbs.service.ServiceImpl;

import com.usst.thumbs.exception.BusinessException;
import com.usst.thumbs.result.ResultType;
import com.usst.thumbs.service.FileService;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public String uploadImage(MultipartFile file) {
        if(file.isEmpty())
            throw new BusinessException(ResultType.PARAM_ERROR,"上传内容为空");
        String contentType = file.getContentType();
        if(contentType==null || !contentType.startsWith("image/"))
            throw new BusinessException(ResultType.PARAM_ERROR,"只能上传文章封面");
        long maxSize = 5*1024*1024;
        if(file.getSize()>maxSize)
            throw new BusinessException(ResultType.PARAM_ERROR,"图片尺寸大小不超过5M");
        try {
            String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    +"-"
                    + UUID.randomUUID().toString().replace("-","").substring(0,8)
                    + ".jpg";
            Path coverDir = Paths.get(uploadDir, "covers");
            Files.createDirectories(coverDir);
            Path targetPath = coverDir.resolve(fileName);
            try (InputStream inputStream = file.getInputStream()) {
                Thumbnails.of(inputStream)
                        .size(600,600)
                        .outputQuality(0.75)
                        .outputFormat("jpg")
                        .toFile(targetPath.toFile());
            }
            return urlPrefix + "/covers/" + fileName;
        } catch (IOException e) {
            throw new BusinessException(ResultType.SYSTEM_ERROR,"文件上传失败");
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
