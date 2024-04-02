package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.PromotionDetailDto;
import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.models.PromotionLine;
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
import java.util.List;

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
    public ResponseEntity<PromotionDto> getPromotionById(@PathVariable Long id) {

        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @PutMapping()
    public ResponseEntity<MessageResponseDto> updatePromotion(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("status") boolean status) {
        PromotionDto promotionDto = new PromotionDto();
        promotionDto.setId(id);
        promotionDto.setName(name);
        promotionDto.setDescription(description);
        promotionDto.setStartDate(startDate);
        promotionDto.setEndDate(endDate);
        promotionDto.setStatus(status);
        try {
            promotionService.updatePromotion(promotionDto);
            return ResponseEntity.ok(new MessageResponseDto("Promotion updated successfully", 200, LocalDateTime.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new MessageResponseDto(e.getMessage(), 400, LocalDateTime.now().toString()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(new MessageResponseDto("Promotion deleted successfully", 200, LocalDateTime.now().toString()));
    }

    @GetMapping
    public ResponseEntity<PageResponse<PromotionDto>> getAllPromotion(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(value = "status", required = false) boolean status) {
        PageResponse<PromotionDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(promotionService.getAllPromotion(page, size, startDate, endDate, status));
        pageResponse.setTotalElements(promotionService.countAllPromotion(startDate, endDate, status));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
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
            @RequestPart("PromotionDetailDto") PromotionDetailDto promotionDetailDto
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

    //get promotion fit the bill
    @GetMapping("/fit-bill")
    public ResponseEntity<List<PromotionLine>> getPromotionFitBill(
            @RequestParam("totalValueBill") double totalValueBill,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("applicableObject") String applicableObject) {

        return ResponseEntity.ok(promotionService.getAllPromotionFitBill(totalValueBill, dateTime, applicableObject));
    }

    @GetMapping("/fit-bill-code")
    public ResponseEntity<PromotionLine> getPromotionLineByCodeAndFitBill(
            @RequestParam("promotionLineCode") String promotionLineCode,
            @RequestParam("totalValueBill") double totalValueBill,
            @RequestParam("dateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
            @RequestParam("applicableObject") String applicableObject) {
        return ResponseEntity.ok(promotionService.getPromotionLineByCodeAndFitBill(promotionLineCode, totalValueBill, dateTime, applicableObject));
    }

    @GetMapping("/line/{id}")
    public ResponseEntity<PromotionLineDto> getPromotionLineById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionLineService.getPromotionLineById(id));
    }

    @GetMapping("/line")
    public ResponseEntity<PageResponse<PromotionLineDto>> getAllPromotionLineFromPromotionId(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam("promotionId") Long promotionId,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(value = "applicableObject", required = false) String applicableObject,
            @RequestParam(value = "typePromotion", required = false) String typePromotion,
            @RequestParam(value = "status", required = false) boolean status) {
        PageResponse<PromotionLineDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(promotionLineService.getAllPromotionLineFromPromotionId(page, size, promotionId, code, startDate, endDate, applicableObject, typePromotion));
        pageResponse.setTotalElements(promotionLineService.countAllPromotionLineFromPromotionId(promotionId, code, startDate, endDate, applicableObject, typePromotion));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);

    }

    @DeleteMapping("/line/{id}")
    public ResponseEntity<MessageResponseDto> deletePromotionLine(@PathVariable Long id) {
        promotionLineService.deletePromotionLine(id);
        return ResponseEntity.ok(new MessageResponseDto("Promotion line deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
    }

}
