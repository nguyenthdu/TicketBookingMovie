package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.models.PromotionLine;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PromotionLineService {
    void createPromotionLine(PromotionLineDto promotionLineDto, MultipartFile multipartFile)throws IOException;
    PromotionLine getPromotionLineById(Long id);
}
