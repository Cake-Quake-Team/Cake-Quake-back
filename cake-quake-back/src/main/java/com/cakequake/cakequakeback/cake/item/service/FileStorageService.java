package com.cakequake.cakequakeback.cake.item.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    // 파일 저장하고 접근 가능한 URL 반환
    String storeFile(MultipartFile file, String folderPath);

    // 파일 삭제
    void deleteFile(String fileUrl);
}
