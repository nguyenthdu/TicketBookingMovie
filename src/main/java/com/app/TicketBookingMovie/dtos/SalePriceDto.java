package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SalePriceDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean status;
    private Set<SalePriceDetailDto> salePriceDetailDtos;
    private LocalDateTime createdDate;
}
