package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PriceDetailDto   {
    private Long id;
    private BigDecimal price;
    private Long priceHeaderId;
    private boolean status;
    private String type; // 'FOOD', 'ROOM' or 'TYPE_SEAT'
    private Long foodId;
    private Long roomId;
    private Long typeSeatId;
    private String name; // Chứa tên của sản phẩm
    private String code; // Chứa mã của sản phẩm
    private LocalDateTime createdDate;
}
