package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionFoodDetailDto  {
    private Long id;
   private Long foodRequired;//đồ ăn cần mua
    private int quantityRequired;//số lượng cần mua
    private Long foodFree;//đồ ăn được tặng
    private int quantityFree;//số lượng được tặng
    private BigDecimal price;//giá sản phẩm được tặng
}
