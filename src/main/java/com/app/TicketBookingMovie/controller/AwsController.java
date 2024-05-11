package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.AwsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = awsService.uploadImage(file);
            return ResponseEntity.ok(new MessageResponse(imageUrl, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getMessage()));
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
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
