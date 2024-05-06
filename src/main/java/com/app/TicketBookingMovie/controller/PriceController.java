package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.dtos.PriceHeaderDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.PriceDetailService;
import com.app.TicketBookingMovie.services.PriceHeaderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("api/price")
public class PriceController {
    private final PriceHeaderService priceHeaderService;
    private final PriceDetailService priceDetailService;

    public PriceController(PriceHeaderService priceHeaderService,
                           PriceDetailService priceDetailService) {
        this.priceHeaderService = priceHeaderService;
        this.priceDetailService = priceDetailService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createSalePrice(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(value = "status", required = false) boolean status) {
        PriceHeaderDto priceHeaderDto = new PriceHeaderDto();
        priceHeaderDto.setName(name);
        priceHeaderDto.setDescription(description);
        priceHeaderDto.setStartDate(startDate);
        priceHeaderDto.setEndDate(endDate);
        priceHeaderDto.setStatus(status);
        try {
            priceHeaderService.createPriceHeader(priceHeaderDto);
            return ResponseEntity.ok(new MessageResponse("Tạo chương trình thay đổi giá thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceHeaderDto> getSalePrice(@PathVariable("id") Long id) {
        return ResponseEntity.ok(priceHeaderService.getPriceHeaderById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateSalePrice(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("status") boolean status) {
        PriceHeaderDto priceHeaderDto = new PriceHeaderDto();
        priceHeaderDto.setId(id);
        priceHeaderDto.setName(name);
        priceHeaderDto.setDescription(description);
        priceHeaderDto.setEndDate(endDate);
        priceHeaderDto.setStatus(status);
        try {
            priceHeaderService.updatePriceHeader(priceHeaderDto);
            return ResponseEntity.ok(new MessageResponse("Cập nhật chương trình thay đổi giá thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }


    @GetMapping
    public ResponseEntity<PageResponse<PriceHeaderDto>> getAllSalePrice(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "startDate", required = false) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) LocalDate endDate) {
        {
            PageResponse<PriceHeaderDto> pageResponse = new PageResponse<>();
            pageResponse.setContent(priceHeaderService.getAllPriceHeader(page, size, code, name, startDate, endDate));
            pageResponse.setTotalElements(priceHeaderService.countAllPriceHeader(code, name, startDate, endDate));
            pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
            pageResponse.setCurrentPage(page);
            pageResponse.setPageSize(size);
            return ResponseEntity.ok(pageResponse);
        }
    }


    @PostMapping("/detail")
    public ResponseEntity<MessageResponse> createPriceDetail(
            @RequestBody Set<PriceDetailDto> priceDetailDto) {
        try {
            priceDetailService.createPriceDetail(priceDetailDto);
            return ResponseEntity.ok(new MessageResponse("Tạo chi tiết chương trình thay đổi giá thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<PriceDetailDto> getSalePriceDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(priceDetailService.getPriceDetail(id));
    }

    @PutMapping("/detail")
    public ResponseEntity<MessageResponse> updatePriceDetail(
            @RequestParam("id") Long id,
            @RequestParam("price") BigDecimal price,
            @RequestParam("status") boolean status) {
        PriceDetailDto priceDetailDto = new PriceDetailDto();
        priceDetailDto.setId(id);
        priceDetailDto.setPrice(price);
        priceDetailDto.setStatus(status);
        try {
            priceDetailService.updatePriceDetail(price, status, id);
            return ResponseEntity.ok(new MessageResponse("Cập nhật chi tiết chương trình thay đổi giá thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/detail")
    public ResponseEntity<PageResponse<PriceDetailDto>> getAllSalePriceDetail(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam("priceHeaderId") Long id,
            @RequestParam(value = "typeDetail", required = false) String typeDetail,
            @RequestParam(value = "foodCode", required = false) String foodCode,
            @RequestParam(value = "roomCode", required = false) String roomCode,
            @RequestParam(value = "typeSeatCode", required = false) String typeSeat
    ) {
        PageResponse<PriceDetailDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(priceDetailService.getAllPriceDetail(page, size, id, typeDetail, foodCode, roomCode, typeSeat));
        pageResponse.setTotalElements(priceDetailService.countAllPriceDetail(id, typeDetail, foodCode, roomCode, typeSeat));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteSalePrice(@PathVariable("id") Long id) {
        try {
            priceHeaderService.deletePriceHeaderById(id);
            return ResponseEntity.ok(new MessageResponse("Xóa chương trình thay đổi giá thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    //delete sale price detail
    @DeleteMapping("/detail/{id}")
    public ResponseEntity<MessageResponse> deleteSalePriceDetail(@PathVariable Long id) {
        try {
            priceDetailService.deletePriceDetail(id);
            return ResponseEntity.ok(new MessageResponse("Xóa chi tiết chương trình thay đổi giá thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    //delete sale price

}
