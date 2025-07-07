package com.cakequake.cakequakeback.cake.item.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3StorageServiceImpl implements FileStorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName = "elasticbeanstalk-ap-northeast-2-853972008946";

    public S3StorageServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String storeFile(MultipartFile file, String folderPath) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;

        // 예: folderPath = "images/shopImages/" → "images/shopImages/uuid.png"
        String key = folderPath + newFileName;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead)); // 퍼블릭 접근 가능하게 설정

            return key; // or amazonS3.getUrl(bucketName, key).toString(); 전체 URL 원할 경우

        } catch (IOException e) {
            throw new RuntimeException("S3 파일 업로드 실패: " + originalFilename, e);
        }
    }


    @Override
    public void deleteFile(String fileUrl) {
        // fileUrl이 key라고 가정 (예: images/cakeImages/uuid.jpg)
        amazonS3.deleteObject(bucketName, fileUrl);
    }
}
