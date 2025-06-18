package com.cakequake.cakequakeback.shop;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CustomImagesUtils {
    // 단일 이미지 저장 + 썸네일 생성
    public String saveImageFile(MultipartFile file, String uploadDir) {
        validateImageFile(file);

        String savedName = generateUniqueFileName(file.getOriginalFilename());
        File uploadPath = getOrCreateUploadDir(uploadDir);

        File saveFile = new File(uploadPath, savedName);

        try {
            file.transferTo(saveFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 오류 발생: " + savedName);
        }

        // 썸네일 생성
        createThumbnail(saveFile, new File(uploadPath, "s_" + savedName));

        return savedName;
    }

    // 다중 이미지 저장 + 썸네일 생성
    public List<String> saveImageFiles(MultipartFile[] files, String uploadDir) {
        List<String> savedFileNames = new ArrayList<>();
        File uploadPath = getOrCreateUploadDir(uploadDir);

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            validateImageFile(file);

            String savedName = generateUniqueFileName(file.getOriginalFilename());
            File saveFile = new File(uploadPath, savedName);

            try {
                file.transferTo(saveFile);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 오류 발생: " + savedName);
            }

            // 썸네일 생성
            createThumbnail(saveFile, new File(uploadPath, "s_" + savedName));
            savedFileNames.add(savedName);
        }

        return savedFileNames;
    }


    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (file.isEmpty() || contentType == null || !contentType.startsWith("image")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE, "이미지 파일이 아닙니다.");
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID().toString() + "_" + originalFilename;
    }

    private File getOrCreateUploadDir(String uploadDir) {
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }
        return uploadPath;
    }

    private void createThumbnail(File source, File target) {
        try {
            Thumbnails.of(source)
                    .size(200, 200)
                    .toFile(target);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "썸네일 생성 실패: " + source.getName());
        }
    }
    public void deleteImageFile(String imageUrl, String uploadDir, String baseUrl) {
        // imageUrl: /shop/Images/uuid_filename.jpg
        // baseUrl: /shop/Images/
        // uploadDir: C:/nginx-1.26.3/html/shop/Images

        if (!imageUrl.startsWith(baseUrl)) {
            // 이 서비스에서 관리하는 이미지가 아님
            return;
        }
        String fileName = imageUrl.substring(baseUrl.length());
        File fileToDelete = new File(uploadDir, fileName);
        File thumbnailToDelete = new File(uploadDir, "s_" + fileName);

        if (fileToDelete.exists()) {
            if (!fileToDelete.delete()) {
                // 삭제 실패 시 로깅 또는 예외 처리
                // throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "원본 파일 삭제 실패: " + fileName);
                System.err.println("원본 파일 삭제 실패: " + fileName);
            }
        }
        if (thumbnailToDelete.exists()) {
            if (!thumbnailToDelete.delete()) {
                // 삭제 실패 시 로깅 또는 예외 처리
                // throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "썸네일 파일 삭제 실패: " + "s_" + fileName);
                System.err.println("썸네일 파일 삭제 실패: " + "s_" + fileName);
            }
        }
    }
}

//saveImageFile(...) → originalName.jpg 저장 및 s_originalName.jpg 썸네일 생성, 저장 이름 반환
//saveImageFiles(...) → 이미지 이름 리스트 반환 (originalName.jpg, ...)