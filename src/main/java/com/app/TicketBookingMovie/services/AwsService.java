package com.app.TicketBookingMovie.services;

import org.springframework.web.multipart.MultipartFile;

public interface AwsService {
    String uploadImage(MultipartFile file) ;
    void deleteImage(String imageUrl);
//    String updateImage(MultipartFile file, String imageUrl) throws IOException;
}
