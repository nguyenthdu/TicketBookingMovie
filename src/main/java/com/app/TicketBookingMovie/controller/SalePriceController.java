package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.SalePriceDetailDto;
import com.app.TicketBookingMovie.dtos.SalePriceDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.SalePriceDetailService;
import com.app.TicketBookingMovie.services.SalePriceService;
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
    private final SalePriceService salePriceService;
    private final SalePriceDetailService salePriceDetailService;

    public SalePriceController(SalePriceService salePriceService,
                               SalePriceDetailService salePriceDetailService) {
        this.salePriceService = salePriceService;
        this.salePriceDetailService = salePriceDetailService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createSalePrice(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("status") boolean status) {
        SalePriceDto salePriceDto = new SalePriceDto();
        salePriceDto.setName(name);
        salePriceDto.setDescription(description);
        salePriceDto.setStartDate(startDate);
        salePriceDto.setEndDate(endDate);
        salePriceDto.setStatus(status);
        try {
            salePriceService.createSalePrice(salePriceDto);
            return ResponseEntity.ok(new MessageResponseDto("Sale price created successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalePriceDto> getSalePrice(@PathVariable("id") Long id) {
        return ResponseEntity.ok(salePriceService.getSalePriceById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponseDto> updateSalePrice(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam("status") boolean status) {
        SalePriceDto salePriceDto = new SalePriceDto();
        salePriceDto.setId(id);
        salePriceDto.setName(name);
        salePriceDto.setDescription(description);
        salePriceDto.setEndDate(endDate);
        salePriceDto.setStatus(status);
        try {
            salePriceService.updateSalePrice(salePriceDto);
            return ResponseEntity.ok(new MessageResponseDto("Sale price updated successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }



    @GetMapping
    public ResponseEntity<PageResponse<SalePriceDto>> getAllSalePrice(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false, defaultValue = "true") Boolean status,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate) {
        {
            PageResponse<SalePriceDto> pageResponse = new PageResponse<>();
            pageResponse.setContent(salePriceService.getAllSalePrice(page, size, code, name, status, startDate, endDate));
            pageResponse.setTotalElements(salePriceService.countAllSalePrice(code, name, status, startDate, endDate));
            pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
            pageResponse.setCurrentPage(page);
            pageResponse.setPageSize(size);
            return ResponseEntity.ok(pageResponse);
        }
    }


    @PostMapping("/detail")
    public ResponseEntity<MessageResponseDto> createSalePriceDetail(@RequestBody Set<SalePriceDetailDto> salePriceDetailDtos) {
        try {
            salePriceDetailService.createSalePriceDetail(salePriceDetailDtos);
            return ResponseEntity.ok(new MessageResponseDto("Sale price detail created successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @GetMapping("/detail/{id}")
    public ResponseEntity<SalePriceDetailDto> getSalePriceDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(salePriceDetailService.getSalePriceDetail(id));
    }
    @PutMapping("/detail/{id}")
    public ResponseEntity<MessageResponseDto> updateStatusSalePriceDetail(@PathVariable Long id) {
        try {
            salePriceDetailService.updateStatusSalePriceDetail(id);
            return ResponseEntity.ok(new MessageResponseDto("Sale price detail updated successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @GetMapping("/{id}/detail")
    public ResponseEntity<Set<SalePriceDetailDto>> getAllSalePriceDetail( @PathVariable Long id) {
        return ResponseEntity.ok(salePriceDetailService.getAllSalePriceDetail(id));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteSalePrice(@PathVariable("id") Long id) {
        try {
            salePriceService.deleteSalePriceById(id);
            return ResponseEntity.ok(new MessageResponseDto("Sale price deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    //delete sale price detail
    @DeleteMapping("/detail/{id}")
    public ResponseEntity<MessageResponseDto> deleteSalePriceDetail(@PathVariable Long id) {
        try {
            salePriceDetailService.deleteSalePriceDetail(id);
            return ResponseEntity.ok(new MessageResponseDto("Sale price detail deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    //delete sale price

}
