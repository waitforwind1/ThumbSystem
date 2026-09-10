package com.usst.thumbs.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileService {
    String uploadImage(MultipartFile multipartFile, HttpServletRequest request);
    String downloadRemoteImage(String url);
}
