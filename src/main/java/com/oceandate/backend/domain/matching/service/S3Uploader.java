package com.oceandate.backend.domain.matching.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket.name}")
    private String bucket;

    public String upload(MultipartFile file) {
        // 원본 파일 이름을 그대로 사용
        String fileName = file.getOriginalFilename().replaceAll("\\s", "_");
        String uuid = UUID.randomUUID().toString();
        String s3FileName = uuid + "_" + fileName;

        // 파일을 S3에 업로드
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, s3FileName, file.getInputStream(), metadata));
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");
        }

        // 업로드된 파일의 URL 반환
        return getFileUrl(s3FileName);
    }

    public void deleteFile(String fileName) {
        amazonS3.deleteObject(bucket, fileName);
        log.info("S3에서 파일 삭제: {}", fileName);
    }

    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(bucket, fileName).toString();
    }
}
