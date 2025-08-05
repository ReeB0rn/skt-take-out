package com.sky.service.impl;

import com.sky.service.CommonService;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private AliOssUtil aliOssUtil;


    /**
     * 上传文件
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) {

        // 获取上传文件名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;

        try {
            return aliOssUtil.upload(file.getBytes(),fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
