package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.services.AwsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/aws")
public class AwsController {
    private final AwsService awsService;

    public AwsController(AwsService awsService) {
        this.awsService = awsService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = awsService.uploadImage(file);
            return ResponseEntity.ok(new MessageResponseDto(imageUrl, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getMessage()));
        }
    }

    @DeleteMapping
    public void deleteImage(
            @RequestParam("imageUrl") String imageUrl
    ) {
        awsService.deleteImage(imageUrl);
    }

//    @PutMapping("/update")
//    public String updateImage(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("imageUrl") String imageUrl
//    ) throws IOException {
//        return awsService.updateImage(file, imageUrl);
//    }
}
