package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionLineService {
    void createPromotionLine(PromotionLineDto promotionLineDto, MultipartFile multipartFile)throws IOException;

    //    @Override
    //    @Transactional
    //    public void createPromotionLine(PromotionLineDto promotionLineDto, MultipartFile multipartFile) throws IOException {
    //        Promotion promotion = promotionService.findPromotionById(promotionLineDto.getPromotionId());
    //        //kểm tra code đã tồn tại chưa
    //        if (promotion.getPromotionLines().stream().anyMatch(line -> line.getCode().equals(promotionLineDto.getCode()))) {
    //            throw new AppException("Mã khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST);
    //        }
    //        checkFileType(multipartFile);
    //        if (multipartFile.getSize() > MAX_SIZE) {
    //            throw new AppException("Kích thước ảnh quá lớn", HttpStatus.BAD_REQUEST);
    //        }
    //        String image = Objects.requireNonNull(multipartFile.getOriginalFilename());
    //        String fileType = image.substring(image.lastIndexOf("."));
    //        String fileName = promotionLineDto.getCode() + "_" + LocalDateTime.now() + fileType;
    //        File file = convertMultiPartFileToFile(multipartFile);
    //        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_PROMOTION, fileName, file));
    //        file.delete();
    //        String uploadLink = amazonS3.getUrl(BUCKET_NAME_PROMOTION, fileName).toString();
    //        PromotionLine promotionLine = modelMapper.map(promotionLineDto, PromotionLine.class);
    //        promotionLine.setImage(uploadLink);
    //
    //        if (!promotionLine.getStartDate().isAfter(promotion.getStartDate()) || !promotionLine.getEndDate().isBefore(promotion.getEndDate())) {
    //            throw new AppException("Thời gian hoạt động  khuyến mãi phải nằm trong thời gian khuyến mãi của: "+promotion.getName()+" là từ ngày: "+promotion.getStartDate()+" đến "+promotion.getEndDate(), HttpStatus.BAD_REQUEST);
    //        }
    //        //check start date and end date
    //        if (promotionLine.getStartDate().isAfter(promotionLine.getEndDate())) {
    //            throw new AppException("Ngày bắt đầu không thể sau ngày kết thúc", HttpStatus.BAD_REQUEST);
    //        }
    //        //check đối tượng áp dung
    //        switch (promotionLineDto.getApplicableObject()) {
    //            case "ALL" -> promotionLine.setApplicableObject(EApplicableObject.ALL);
    //            case "LEVEL_NORMAL" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_NORMAL);
    //            case "LEVEL_SILVER" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_SILVER);
    //            case "LEVEL_GOLD" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_GOLD);
    //            case "LEVEL_PLATINUM" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_PLATINUM);
    //            default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
    //        }
    //        if (promotionLineDto.getTypePromotion().equals("GIFT")) {
    //            promotionLine.setTypePromotion(ETypePromotion.GIFT);
    //            PromotionDetail promotionDetail = promotionDetailService.createPromotionDetailGift(promotionLineDto.getPromotionDetailDtos());
    //            promotionLine.setPromotionDetail(promotionDetail);
    //            //trừ đi số lượng sản phẩm
    //            promotionDetail.getFood().setQuantity(promotionDetail.getFood().getQuantity() - promotionDetail.getMaxValue());
    //
    //        } else if (promotionLineDto.getTypePromotion().equals("DISCOUNT")) {
    //            promotionLine.setTypePromotion(ETypePromotion.DISCOUNT);
    //            PromotionDetail promotionDetail = promotionDetailService.createPromotionDetailDiscount(promotionLineDto.getPromotionDetailDtos());
    //            promotionLine.setPromotionDetail(promotionDetail);
    //
    //        } else {
    //            throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
    //
    //        }
    //        //nếu status của promotion là false thì không được tạo promotion line
    //        if(!promotion.isStatus() && promotionLine.isStatus()){
    //            throw new AppException("Không thể kích hoạt hoạt động khuyến mãi khi chương trình khuyến mãi không hoạt động", HttpStatus.BAD_REQUEST);
    //
    //        }
    //        if(promotion.getEndDate().isBefore(LocalDateTime.now())){
    //            throw new AppException("Không thể tạo khuyến mãi khi chương trình khuyến mãi đã kết thúc", HttpStatus.BAD_REQUEST);
    //        }
    //        promotionLineRepository.save(promotionLine);
    //        promotion.getPromotionLines().add(promotionLine);
    //
    //    }
    @Transactional
    void createPromotionLine(PromotionLineDto promotionLineDto);

    PromotionLineDto getPromotionLineById(Long promotionLineId);
    List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion);
    long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion);
    void deletePromotionLine(Long promotionLineId);
}
