package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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
            throw new AppException("Định dạng ảnh phải là jpg hoặc png", HttpStatus.BAD_REQUEST);
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

    }

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
@Override
@Transactional
public void createPromotionLine(PromotionLineDto promotionLineDto) {
    Promotion promotion = promotionService.findPromotionById(promotionLineDto.getPromotionId());
    // Kiểm tra code đã tồn tại chưa
    if (promotion.getPromotionLines().stream().anyMatch(line -> line.getCode().equals(promotionLineDto.getCode()))) {
        throw new AppException("Mã khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST);
    }
    PromotionLine promotionLine = modelMapper.map(promotionLineDto, PromotionLine.class);

    if (!promotionLine.getStartDate().isAfter(promotion.getStartDate()) || !promotionLine.getEndDate().isBefore(promotion.getEndDate())) {
        throw new AppException("Thời gian hoạt động khuyến mãi phải nằm trong thời gian khuyến mãi của: " + promotion.getName() + " là từ ngày: " + promotion.getStartDate() + " đến " + promotion.getEndDate(), HttpStatus.BAD_REQUEST);
    }

    // Check start date and end date
    if (promotionLine.getStartDate().isAfter(promotionLine.getEndDate())) {
        throw new AppException("Ngày bắt đầu không thể sau ngày kết thúc", HttpStatus.BAD_REQUEST);
    }

    // Check đối tượng áp dung
    switch (promotionLineDto.getApplicableObject()) {
        case "ALL" -> promotionLine.setApplicableObject(EApplicableObject.ALL);
        case "LEVEL_NORMAL" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_NORMAL);
        case "LEVEL_SILVER" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_SILVER);
        case "LEVEL_GOLD" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_GOLD);
        case "LEVEL_PLATINUM" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_PLATINUM);
        default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
    }

    if (promotionLineDto.getTypePromotion().equals("GIFT")) {
        promotionLine.setTypePromotion(ETypePromotion.GIFT);
        PromotionDetail promotionDetail = promotionDetailService.createPromotionDetailGift(promotionLineDto.getPromotionDetailDto());
        promotionLine.setPromotionDetail(promotionDetail);
        // Trừ đi số lượng sản phẩm
        promotionDetail.getFood().setQuantity(promotionDetail.getFood().getQuantity() - promotionDetail.getMaxValue());
    } else if (promotionLineDto.getTypePromotion().equals("DISCOUNT")) {
        promotionLine.setTypePromotion(ETypePromotion.DISCOUNT);
        PromotionDetail promotionDetail = promotionDetailService.createPromotionDetailDiscount(promotionLineDto.getPromotionDetailDto());
        promotionLine.setPromotionDetail(promotionDetail);
    } else {
        throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
    }

    // Nếu status của promotion là false thì không được tạo promotion line
    if (!promotion.isStatus() && promotionLine.isStatus()) {
        throw new AppException("Không thể kích hoạt hoạt động khuyến mãi khi chương trình khuyến mãi không hoạt động", HttpStatus.BAD_REQUEST);
    }

    if (promotion.getEndDate().isBefore(LocalDateTime.now())) {
        throw new AppException("Không thể tạo khuyến mãi khi chương trình khuyến mãi đã kết thúc", HttpStatus.BAD_REQUEST);
    }


    promotionLineRepository.save(promotionLine);
    promotion.getPromotionLines().add(promotionLine);
}


    @Override
    public PromotionLineDto getPromotionLineById(Long promotionLineId) {
        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineId).orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi với id: " + promotionLineId, HttpStatus.NOT_FOUND));
        //lấy promotiondetail
        PromotionDetailDto promotionDetailDto = promotionDetailService.getPromotionDetailByPromotionLineId(promotionLineId);
        PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
        promotionLineDto.setPromotionDetailDto(promotionDetailDto);
        return promotionLineDto;
    }

    @Override
    public List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionLine> promotionLines;


        if (promotionId != null ) {
            promotionLines = promotionLineRepository.findAllByPromotionId(promotionId, pageable);
        } else if (promotionLineCode != null && !promotionLineCode.isEmpty()) {
            promotionLines = promotionLineRepository.findAllByCode(promotionLineCode, pageable);
        } else if (startDate != null && endDate != null) {
            promotionLines = promotionLineRepository.findAllByStartDateAndEndDate(startDate, endDate, pageable);
        } else if (applicableObject != null && !applicableObject.isEmpty()) {
            switch (applicableObject) {
                case "ALL" ->
                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.ALL, pageable);
                case "LEVEL_NORMAL" ->
                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_NORMAL, pageable);
                case "LEVEL_SILVER" ->
                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_SILVER, pageable);
                case "LEVEL_GOLD" ->
                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_GOLD, pageable);
                case "LEVEL_PLATINUM" ->
                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_PLATINUM, pageable);
                default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
            }
        } else if (typePromotion != null && !typePromotion.isEmpty()) {
            switch (typePromotion) {
                case "GIFT" ->
                        promotionLines = promotionLineRepository.findAllByTypePromotion(ETypePromotion.GIFT, pageable);
                case "DISCOUNT" ->
                        promotionLines = promotionLineRepository.findAllByTypePromotion(ETypePromotion.DISCOUNT, pageable);
                default -> throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
            }
        } else {
            promotionLines = promotionLineRepository.findAll(pageable);
        }
        return promotionLines.map(promotionLine -> {
            PromotionDetailDto promotionDetailDto = promotionDetailService.getPromotionDetailByPromotionLineId(promotionLine.getId());
            PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
            promotionLineDto.setPromotionDetailDto(promotionDetailDto);
            return promotionLineDto;
        }).getContent();
    }

    @Override
    public long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion) {
        //đếm số lương phẩn tử trả về
        if (promotionId != null) {
            return promotionLineRepository.countByPromotionId(promotionId);
        } else if (promotionLineCode != null && !promotionLineCode.isEmpty()) {
            return promotionLineRepository.countByCode(promotionLineCode);
        } else if (startDate != null && endDate != null) {
            return promotionLineRepository.countAllByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate);
        } else if (applicableObject != null && !applicableObject.isEmpty()) {
            switch (applicableObject) {
                case "ALL" -> {
                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.ALL);
                }
                case "LEVEL_NORMAL" ->
                {
                    return     promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_NORMAL);
                }
                case "LEVEL_SILVER" ->
                {
                    return      promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_SILVER);
                }
                case "LEVEL_GOLD" -> {
                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_GOLD);
                }
                case "LEVEL_PLATINUM" -> {
                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_PLATINUM);
                }
                default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
            }
        } else if (typePromotion != null && !typePromotion.isEmpty()) {
            switch (typePromotion) {
                case "GIFT" -> {
                    return promotionLineRepository.countAllByTypePromotion(ETypePromotion.GIFT);
                }
                case "DISCOUNT" -> {
                    return promotionLineRepository.countAllByTypePromotion(ETypePromotion.DISCOUNT);
                }
                default -> throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
            }

        } else {
            return promotionLineRepository.count();
        }
    }

    @Override
    public void deletePromotionLine(Long promotionLineId) {
        //nếu như promotion line đã bắt đầu thì không thể xóa
        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineId).orElseThrow(() -> new AppException("Promotion line not found", HttpStatus.NOT_FOUND));
        if (LocalDateTime.now().isAfter(promotionLine.getStartDate())) {
            throw new AppException("Không thể xóa khuyến mãi khi đã bắt đầu", HttpStatus.BAD_REQUEST);
        }
        promotionLineRepository.deleteById(promotionLineId);

    }

}
