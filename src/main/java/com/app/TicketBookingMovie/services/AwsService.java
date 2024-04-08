package com.app.TicketBookingMovie.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AwsService {
    String uploadImage(MultipartFile file) throws IOException;
    void deleteImage(String imageUrl);
//    String updateImage(MultipartFile file, String imageUrl) throws IOException;
}
