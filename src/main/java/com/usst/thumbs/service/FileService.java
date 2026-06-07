package com.usst.thumbs.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileService {
    String uploadImage(MultipartFile multipartFile);
    String downloadRemoteImage(String url);
}
