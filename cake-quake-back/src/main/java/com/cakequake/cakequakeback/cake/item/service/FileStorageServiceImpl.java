package com.cakequake.cakequakeback.cake.item.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    String uploadDir = "C:\\nginx-1.26.3\\html\\upload";

    @Override
    public String storeFile(MultipartFile file) {
        // 원본 파일명 정리
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        // 확장자 포함 랜덤 UUID 파일명 생성
        String fileExtension = "";
        int extIndex = originalFilename.lastIndexOf(".");
        if (extIndex > 0) {
            fileExtension = originalFilename.substring(extIndex);
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            Path targetLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(targetLocation);

            Path filePath = targetLocation.resolve(newFileName);
            file.transferTo(filePath.toFile());

            // 저장 후 URL 경로 리턴 (로컬일 경우 그냥 파일명이나 URL 기본 경로 합쳐서 반환)
            return "/upload/" + newFileName;

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + originalFilename, e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            // fileUrl 에서 파일명만 추출 (예: "/uploads/abc.jpg" -> "abc.jpg")
            String fileName = Paths.get(fileUrl).getFileName().toString();
            Path filePath = Paths.get(uploadDir).resolve(fileName).toAbsolutePath().normalize();

            Files.deleteIfExists(filePath);

        } catch (IOException e) {
            throw new RuntimeException("파일 삭제 실패: " + fileUrl, e);
        }
    }
}

