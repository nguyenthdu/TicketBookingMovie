package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.PromotionDetailDto;
import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.services.PromotionLineService;
import com.app.TicketBookingMovie.services.PromotionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("api/promotion")
public class PromotionController {
    private final PromotionService promotionService;
    private final PromotionLineService promotionLineService;

    public PromotionController(PromotionService promotionService, PromotionLineService promotionLineService) {
        this.promotionService = promotionService;
        this.promotionLineService = promotionLineService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createPromotion(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("status") boolean status) {
        PromotionDto promotionDto = new PromotionDto();
        promotionDto.setName(name);
        promotionDto.setDescription(description);
        promotionDto.setStartDate(startDate);
        promotionDto.setEndDate(endDate);
        promotionDto.setStatus(status);
        try {
            promotionService.createPromotion(promotionDto);
            return ResponseEntity.ok(new MessageResponseDto("Promotion created successfully", 200, LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new MessageResponseDto(e.getMessage(), 400, LocalDateTime.now().toString()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Promotion> getPromotionById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PostMapping("/line")
    public ResponseEntity<MessageResponseDto> createPromotionLine(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("typePromotion") String typePromotion,
            @RequestParam("applicableObject") String applicableObject,
            @RequestParam("usePerUser") Long usePerUser,
            @RequestParam("usePerPromotion") Long usePerPromotion,
            @RequestParam("promotionId") Long promotionId,
            @RequestParam("status") boolean status,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestPart("PromotionDetailDto") Set<PromotionDetailDto> promotionDetailDto
    ) {
        PromotionLineDto promotionLineDto = new PromotionLineDto();
        promotionLineDto.setCode(code);
        promotionLineDto.setName(name);
        promotionLineDto.setDescription(description);
        promotionLineDto.setStartDate(startDate);
        promotionLineDto.setEndDate(endDate);
        promotionLineDto.setTypePromotion(typePromotion);
        promotionLineDto.setApplicableObject(applicableObject);
        promotionLineDto.setUsePerUser(usePerUser);
        promotionLineDto.setUsePerPromotion(usePerPromotion);
        promotionLineDto.setPromotionId(promotionId);
        promotionLineDto.setStatus(status);
        promotionLineDto.setPromotionDetailDtos(promotionDetailDto);

        try {
            promotionLineService.createPromotionLine(promotionLineDto, image);
            return ResponseEntity.ok(new MessageResponseDto("Promotion line created successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
