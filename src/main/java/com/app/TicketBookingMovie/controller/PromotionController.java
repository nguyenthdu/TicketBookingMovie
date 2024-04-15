package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.PromotionLineService;
import com.app.TicketBookingMovie.services.PromotionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
            return ResponseEntity.ok(new MessageResponseDto("Tạo chương trình khuyến mãi thành công", HttpStatus.OK.value(), Instant.now().toString()));
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
            return ResponseEntity.ok(new MessageResponseDto("Cập nhật chương trình khuyến mãi thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new MessageResponseDto(e.getMessage(), 400, LocalDateTime.now().toString()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.ok(new MessageResponseDto("Xóa chương trình khuyến mãi thành công", HttpStatus.OK.value(), Instant.now().toString()));
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
    public ResponseEntity<MessageResponseDto> createPromotionLine(@RequestBody PromotionLineDto promotionLineDto) {
        try {
            promotionLineService.createPromotionLine(promotionLineDto);
            return ResponseEntity.ok(new MessageResponseDto("Tạo hoạt động khuyến mãi thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
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
            @RequestParam(value = "typePromotion", required = false) String typePromotion) {
        PageResponse<PromotionLineDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(promotionLineService.getAllPromotionLineFromPromotionId(page, size, promotionId, code, startDate, endDate, typePromotion));
        pageResponse.setTotalElements(promotionLineService.countAllPromotionLineFromPromotionId(promotionId, code, startDate, endDate, typePromotion));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);

    }

    @PutMapping("/line")
    public ResponseEntity<MessageResponseDto> updatePromotionLine(
            @RequestParam("id") Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) String image,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(value = "status", required = false) boolean status

    ) {
        PromotionLineDto promotionLineDto = new PromotionLineDto();
        promotionLineDto.setId(id);
        promotionLineDto.setName(name);
        promotionLineDto.setDescription(description);
        promotionLineDto.setImage(image);
        promotionLineDto.setStartDate(startDate);
        promotionLineDto.setEndDate(endDate);
        promotionLineDto.setStatus(status);
        try {
            promotionLineService.updatePromotionLine(promotionLineDto);
            return ResponseEntity.ok(new MessageResponseDto("Cập nhật hoạt động khuyến mãi thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("/line/{id}")
    public ResponseEntity<MessageResponseDto> deletePromotionLine(@PathVariable Long id) {
        promotionLineService.deletePromotionLine(id);
        return ResponseEntity.ok(new MessageResponseDto("Xóa hoạt động khuyến mãi thành công", HttpStatus.OK.value(), Instant.now().toString()));
    }

    // lấy danh sách promtion line đang hoạt động phù hợp với giá trị hóa đơn
    @GetMapping("/line_discount/active")
    public ResponseEntity<PromotionLineDto> showPromotionLineDiscountMatchInvoice(@RequestParam("totalPrice") BigDecimal totalPrice) {
        return ResponseEntity.ok(promotionLineService.showPromotionLineDiscountMatchInvoice(totalPrice));
    }

    @GetMapping("/line_food/active")
    public ResponseEntity<PromotionLineDto> showPromotionLineFoodMatchInvoice(@RequestParam("foodId") List<Long> foodId, @RequestParam("cinemaId")Long cinemaId) {
        return ResponseEntity.ok(promotionLineService.showPromotionLineFoodMatchInvoice(foodId, cinemaId));
    }

    @GetMapping("/line_ticket/active")
    public ResponseEntity<PromotionLineDto> showPromotionLineTicketMatchInvoice(@RequestParam("seatId") List<Long> seatId
            , @RequestParam("showTimeId") Long showTimeId) {
        return ResponseEntity.ok(promotionLineService.showPromotionLineTicketMatchInvoice(seatId, showTimeId));
    }

}
