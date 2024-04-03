package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.util.List;
@Data
public class ResponeInvoiceDetail {
    private CinemaDto cinemaDto;
    private RoomDto roomDto;
    private MovieDto movieDto;
    private ShowTimeDto showTimeDto;
    private UserDto userDto;
    private List<InvoiceFoodDetailDto> invoiceFoodDetailDtos;
    private List<InvoiceTicketDetailDto> invoiceTicketDetailDtos;
}
