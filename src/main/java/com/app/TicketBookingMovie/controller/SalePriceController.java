package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.dtos.PriceHeaderDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.PriceDetailService;
import com.app.TicketBookingMovie.services.PriceHeaderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequestMapping("api/salePrice")
public class SalePriceController {
    private final PriceHeaderService priceHeaderService;
    private final PriceDetailService priceDetailService;

    public SalePriceController(PriceHeaderService priceHeaderService,
                               PriceDetailService priceDetailService) {
        this.priceHeaderService = priceHeaderService;
        this.priceDetailService = priceDetailService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createSalePrice(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("status") boolean status) {
        PriceHeaderDto priceHeaderDto = new PriceHeaderDto();
        priceHeaderDto.setName(name);
        priceHeaderDto.setDescription(description);
        priceHeaderDto.setStartDate(startDate);
        priceHeaderDto.setEndDate(endDate);
        priceHeaderDto.setStatus(status);
        try {
            priceHeaderService.createPriceHeader(priceHeaderDto);
            return ResponseEntity.ok(new MessageResponseDto("Sale price created successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceHeaderDto> getSalePrice(@PathVariable("id") Long id) {
        return ResponseEntity.ok(priceHeaderService.getPriceHeaderById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponseDto> updateSalePrice(
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
            return ResponseEntity.ok(new MessageResponseDto("Sale price updated successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }


    @GetMapping
    public ResponseEntity<PageResponse<PriceHeaderDto>> getAllSalePrice(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false, defaultValue = "true") Boolean status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        {
            PageResponse<PriceHeaderDto> pageResponse = new PageResponse<>();
            pageResponse.setContent(priceHeaderService.getAllPriceHeader(page, size, code, name, status, startDate, endDate));
            pageResponse.setTotalElements(priceHeaderService.countAllPriceHeader(code, name, status, startDate, endDate));
            pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
            pageResponse.setCurrentPage(page);
            pageResponse.setPageSize(size);
            return ResponseEntity.ok(pageResponse);
        }
    }


    @PostMapping("/detail")
    public ResponseEntity<MessageResponseDto> createSalePriceDetail(@RequestBody Set<PriceDetailDto> priceDetailDtos) {
        try {
            priceDetailService.createPriceDetail(priceDetailDtos);
            return ResponseEntity.ok(new MessageResponseDto("Sale price detail created successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<PriceDetailDto> getSalePriceDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(priceDetailService.getPriceDetail(id));
    }

    @PutMapping("/detail")
    public ResponseEntity<MessageResponseDto> updatePriceDetail(
            @RequestParam("id") Long id,
            @RequestParam("price") double price,
            @RequestParam("status") boolean status) {
        PriceDetailDto priceDetailDto = new PriceDetailDto();
        priceDetailDto.setId(id);
        priceDetailDto.setPrice(price);
        priceDetailDto.setStatus(status);
        try {
            priceDetailService.updatePriceDetail(priceDetailDto);
            return ResponseEntity.ok(new MessageResponseDto("Sale price detail updated successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<Set<PriceDetailDto>> getAllSalePriceDetail(@PathVariable Long id) {
        return ResponseEntity.ok(priceDetailService.getAllPriceDetail(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteSalePrice(@PathVariable("id") Long id) {
        try {
            priceHeaderService.deletePriceHeaderById(id);
            return ResponseEntity.ok(new MessageResponseDto("Sale price deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    //delete sale price detail
    @DeleteMapping("/detail/{id}")
    public ResponseEntity<MessageResponseDto> deleteSalePriceDetail(@PathVariable Long id) {
        try {
            priceDetailService.deletePriceDetail(id);
            return ResponseEntity.ok(new MessageResponseDto("Sale price detail deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    //delete sale price

}
