package com.cakequake.cakequakeback.common.utils;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/*
    이미지 파읠의 업로드 경로는 각 서비스에서 필요한 걸로 받아와서, 폴더 생성 후 실제 로컬에 파일 저장.
 */
@Component
public class CustomImageUtils {

    // 이미지 하나만 저장할 때
    public String saveImageFile(MultipartFile file, String uploadDir) {
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs(); // 디렉토리 없으면 생성
        }

        // 파일이 비어있으면
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);   // 610
        }

        String contentType = file.getContentType();

        // 이미지 타입 확인
        if (contentType == null || !contentType.startsWith("image")) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        String originalName = file.getOriginalFilename();
        String uuid = UUID.randomUUID().toString();
        String savedName = uuid + "_" + originalName;

        File saveFile = new File(uploadDir, savedName);

        try {
            // 실제 파일 저장
            file.transferTo(saveFile);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 오류 발생: " + savedName);
        }

        return savedName;
    }
}
