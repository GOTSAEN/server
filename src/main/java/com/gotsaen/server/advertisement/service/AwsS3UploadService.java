package com.gotsaen.server.advertisement.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.gotsaen.server.advertisement.entity.Advertisement;
import com.gotsaen.server.advertisement.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3UploadService {
    private static final String S3_BUCKET_DIRECTORY_NAME = "images";

    private final AmazonS3Client amazonS3Client;
    private final AdvertisementRepository advertisementRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public List<String> uploadImages(List<MultipartFile> multipartFiles) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            // 메타데이터 설정
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setContentLength(file.getSize());

            // 실제 S3 bucket 디렉토리명 설정
            // 파일명 중복을 방지하기 위한 UUID 추가
            String fileName = S3_BUCKET_DIRECTORY_NAME + "/" + UUID.randomUUID() + "." + file.getOriginalFilename();

            try (InputStream inputStream = file.getInputStream()) {
                amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));

                String imageUrl = amazonS3Client.getUrl(bucket, fileName).toString();
                imageUrls.add(imageUrl);
            } catch (IOException e) {
                log.error("S3 파일 업로드에 실패했습니다. {}", e.getMessage());
                throw new IllegalStateException("S3 파일 업로드에 실패했습니다.");
            }
        }
        return imageUrls;
    }
}
