package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class SalePriceHeaderDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private boolean status;
    private Set<Long> salePriceDetailsId;
}
