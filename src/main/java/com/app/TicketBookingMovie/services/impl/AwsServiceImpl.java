package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.services.AwsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class AwsServiceImpl implements AwsService {

    private final AmazonS3 amazonS3;
    @Value("${S3_BUCKET_NAME_MOVIE}")
    private String BUCKET_NAME_MOVIE;
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB


    public AwsServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String uploadImage(MultipartFile file)   {
        checkFileType(file);
        checkFileSize(file);
        try {
            File convertedFile = convertMultiPartFileToFile(file);
            String fileName = generateFileName(file);
            amazonS3.putObject(BUCKET_NAME_MOVIE, fileName, convertedFile);
            convertedFile.delete();
            return amazonS3.getUrl(BUCKET_NAME_MOVIE, fileName).toString();
        } catch (IOException e) {
            throw new AppException("Lỗi khi upload hình ảnh", HttpStatus.BAD_REQUEST);
        }


    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        String imageKey = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        imageKey = imageKey.replace("%3A", ":");
        amazonS3.deleteObject(BUCKET_NAME_MOVIE, imageKey);

    }


    private void checkFileType(MultipartFile file) {
        String fileName = Objects.requireNonNull(file.getOriginalFilename());
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!fileType.equals(".jpg") && !fileType.equals(".png"))
            throw new AppException("Chỉ hỗ trợ file ảnh định dạng jpg hoặc png", HttpStatus.BAD_REQUEST);
    }

    private File convertMultiPartFileToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        }
        return convertedFile;
    }

    private void checkFileSize(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new AppException("Kích thước file ảnh quá lớn, vui lòng chọn file ảnh có kích thước nhỏ hơn 10MB", HttpStatus.BAD_REQUEST);
        }
    }


    private String generateFileName(MultipartFile file) {
        return "image_" + UUID.randomUUID() + getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
