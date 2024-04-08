package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.services.AwsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/aws")
public class AwsController {
    private final AwsService awsService;

    public AwsController(AwsService awsService) {
        this.awsService = awsService;
    }

    @PostMapping("/upload")
    public String uploadImage(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return awsService.uploadImage(file);
    }

//    @PutMapping("/update")
//    public String updateImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("imageUrl") String imageUrl
//    ) throws IOException {
//        return awsService.updateImage(file, imageUrl);
//    }
}
