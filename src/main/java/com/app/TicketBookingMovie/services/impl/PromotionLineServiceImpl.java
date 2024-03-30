package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.app.TicketBookingMovie.dtos.PromotionDetailDto;
import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionDetail;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.models.enums.EApplicableObject;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.services.PromotionDetailService;
import com.app.TicketBookingMovie.services.PromotionLineService;
import com.app.TicketBookingMovie.services.PromotionService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;

@Service
public class PromotionLineServiceImpl implements PromotionLineService {
    private final PromotionLineRepository promotionLineRepository;
    private final PromotionDetailService promotionDetailService;
    private final PromotionService promotionService;
    private final ModelMapper modelMapper;
    @Value("${S3_BUCKET_NAME_PROMOTION}")
    private String BUCKET_NAME_PROMOTION;
    private final AmazonS3 amazonS3;
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    public PromotionLineServiceImpl(PromotionLineRepository promotionLineRepository, PromotionDetailService promotionDetailService, PromotionService promotionService, ModelMapper modelMapper, AmazonS3 amazonS3) {
        this.promotionLineRepository = promotionLineRepository;
        this.promotionDetailService = promotionDetailService;
        this.promotionService = promotionService;
        this.modelMapper = modelMapper;
        this.amazonS3 = amazonS3;
    }

    public void checkFileType(MultipartFile multipartFile) {
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!fileType.equals(".jpg") && !fileType.equals(".png")) {
            throw new AppException("Only .jpg and .png files are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }

    @Override
    public void createPromotionLine(PromotionLineDto promotionLineDto, MultipartFile multipartFile) throws IOException {
        Promotion promotion = promotionService.getPromotionById(promotionLineDto.getPromotionId());

        checkFileType(multipartFile);
        if (multipartFile.getSize() > MAX_SIZE) {
            throw new AppException("File size is too large", HttpStatus.BAD_REQUEST);
        }
        String image = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = image.substring(image.lastIndexOf("."));
        String fileName = promotionLineDto.getCode() + "_" + LocalDateTime.now() + fileType;
        File file = convertMultiPartFileToFile(multipartFile);
        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_PROMOTION, fileName, file));
        file.delete();
        String uploadLink = amazonS3.getUrl(BUCKET_NAME_PROMOTION, fileName).toString();
        PromotionLine promotionLine = modelMapper.map(promotionLineDto, PromotionLine.class);
        promotionLine.setImage(uploadLink);
    // Khởi tạo promotionDetails nếu chưa tồn tại
        if (promotionLine.getPromotionDetails() == null) {
            promotionLine.setPromotionDetails(new HashSet<>());
        }
        // Kiểm tra trùng thời gian
        LocalDateTime newStartDate = promotionLine.getStartDate();
        LocalDateTime newEndDate = promotionLine.getEndDate();
        for (PromotionLine existingPromotionLine : promotion.getPromotionLines()) {
            LocalDateTime existingStartDate = existingPromotionLine.getStartDate();
            LocalDateTime existingEndDate = existingPromotionLine.getEndDate();

            if ((newStartDate.isAfter(existingStartDate) && newStartDate.isBefore(existingEndDate)) ||
                    (newEndDate.isAfter(existingStartDate) && newEndDate.isBefore(existingEndDate)) ||
                    (newStartDate.isEqual(existingStartDate) || newEndDate.isEqual(existingEndDate))) {
                throw new AppException("Another promotion line with conflicting time already exists in this promotion", HttpStatus.BAD_REQUEST);
            }
        }
        if (!promotionLine.getStartDate().isAfter(promotion.getStartDate()) || !promotionLine.getEndDate().isBefore(promotion.getEndDate())) {
            throw new AppException("The start date and end date must be within the promotion start date and end date", HttpStatus.BAD_REQUEST);
        }
        //check start date and end date
        if (promotionLine.getStartDate().isAfter(promotionLine.getEndDate())) {
            throw new AppException("The end date must be after the start date", HttpStatus.BAD_REQUEST);
        }
        //check đối tượng áp dung
        switch (promotionLineDto.getApplicableObject()) {
            case "ALL" -> promotionLine.setApplicableObject(EApplicableObject.ALL);
            case "MEMBERSHIP" -> promotionLine.setApplicableObject(EApplicableObject.MEMBERSHIP);
            case "LEVEL_NORMAL" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_NORMAL);
            case "LEVEL_SILVER" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_SILVER);
            case "LEVEL_GOLD" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_GOLD);
            case "LEVEL_PLATINUM" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_PLATINUM);
            default -> throw new AppException("Applicable object is invalid", HttpStatus.BAD_REQUEST);
        }
        if (promotionLineDto.getTypePromotion().equals("GIFT")) {
            promotionLine.setTypePromotion(ETypePromotion.GIFT);
            for (PromotionDetailDto promotionDetailDto : promotionLineDto.getPromotionDetailDtos()) {
                PromotionDetail pdetail = promotionDetailService.createPromotionDetailGift(promotionDetailDto);
                promotionLine.getPromotionDetails().add(pdetail);
            }
        } else if (promotionLineDto.getTypePromotion().equals("DISCOUNT")) {
            promotionLine.setTypePromotion(ETypePromotion.DISCOUNT);
            for (PromotionDetailDto promotionDetailDto : promotionLineDto.getPromotionDetailDtos()) {
                PromotionDetail pdetail = promotionDetailService.createPromotionDetailDiscount(promotionDetailDto);
                promotionLine.getPromotionDetails().add(pdetail);
            }
        } else {
            throw new AppException("Type promotion is invalid", HttpStatus.BAD_REQUEST);

        }
        promotionLineRepository.save(promotionLine);
        promotion.getPromotionLines().add(promotionLine);

    }

    @Override
    public PromotionLine getPromotionLineById(Long id) {
        return promotionLineRepository.findById(id).orElseThrow(() -> new AppException("Promotion line not found", HttpStatus.NOT_FOUND));
    }
}
