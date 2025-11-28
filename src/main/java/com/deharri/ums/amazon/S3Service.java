package com.deharri.ums.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    // Upload file to S3 bucket
    public String uploadFile(MultipartFile file, String filePath) {
        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, filePath, file.getInputStream(), null));
            return "File uploaded successfully: " + file.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error uploading file";
        }
    }

    // Download file from S3 bucket
    public S3Object downloadFile(String fileName) {
        return amazonS3.getObject(bucketName, fileName);
    }

    public void deleteFile(String oldPictureUrl) {
        amazonS3.deleteObject(bucketName, oldPictureUrl);
    }

    public String generateFileName(MultipartFile file, UUID userId) {
        return  userId + "-" + LocalDateTime.now() + "." + file.getOriginalFilename().split(".")[1];
    }

    public URL generatePresignedUrl(String key, long minutes) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = System.currentTimeMillis() + 1000 * 60 * minutes;
        expiration.setTime(expTimeMillis);

        return amazonS3.generatePresignedUrl(bucketName, key, expiration);
    }

    public String generateFileKey(UUID userId, String originalFileName) {
        String uuid = UUID.randomUUID().toString();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        LocalDate today = LocalDate.now();

        return String.format(
                "deharri/users/%s/%d/%02d/%02d/%s.%s",
                userId,
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                uuid,
                extension
        );
    }
}