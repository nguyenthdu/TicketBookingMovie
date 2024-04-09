package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
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
    private  String BUCKET_NAME_MOVIE;
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB


    public AwsServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String uploadImage(MultipartFile file) throws IOException {
        checkFileType(file);
        checkFileSize(file);
        File imageFile = convertMultiPartFileToFile(file);
        String fileName = generateFileName(file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(BUCKET_NAME_MOVIE, fileName, imageFile);
        imageFile.delete();
        return amazonS3.getUrl(BUCKET_NAME_MOVIE, fileName).toString();
    }

    @Override
    public void deleteImage(String imageUrl) {
        if(imageUrl == null || imageUrl.isEmpty()){
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
            throw new AppException("Only .jpg and .png files are allowed", HttpStatus.BAD_REQUEST);
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
            throw new AppException("File size is too large. must < 10mb", HttpStatus.BAD_REQUEST);
        }
    }


    private String generateFileName(MultipartFile file) {
        return "image_" + UUID.randomUUID() + getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
