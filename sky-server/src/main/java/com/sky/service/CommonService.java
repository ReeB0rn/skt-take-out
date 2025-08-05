package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

public interface CommonService {

    /**
     * 上传文件
     * @param file
     * @return
     */
    public String upload(MultipartFile file);
}
