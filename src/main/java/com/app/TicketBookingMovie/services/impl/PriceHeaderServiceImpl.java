package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceHeaderDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PriceDetail;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.repository.PriceHeaderRepository;
import com.app.TicketBookingMovie.services.PriceHeaderService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class PriceHeaderServiceImpl implements PriceHeaderService {
    private final PriceHeaderRepository priceHeaderRepository;
    private final ModelMapper modelMapper;

    public String randomCode() {
        return "GI" + LocalDateTime.now().getNano();
    }

    public PriceHeaderServiceImpl(PriceHeaderRepository priceHeaderRepository, ModelMapper modelMapper) {
        this.priceHeaderRepository = priceHeaderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createPriceHeader(PriceHeaderDto priceHeaderDto) {
        // Parse the startDate and endDate from the priceHeaderDto
        LocalDateTime startDate = priceHeaderDto.getStartDate();
        LocalDateTime endDate = priceHeaderDto.getEndDate();

        // Check if the start date is in the past
        if (startDate.isBefore(LocalDateTime.now()) || startDate.getDayOfMonth() == LocalDate.now().getDayOfMonth()) {
            throw new AppException("Ngày bắt đầu phải là ngày tiếp theo", HttpStatus.BAD_REQUEST);
        }

        // Check if the end date is after the start date
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new AppException("Ngày kết thúc phải sau bắt đầu", HttpStatus.BAD_REQUEST);
        }

        // Check if the time period is already occupied
        boolean exists = priceHeaderRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
        if (exists) {
            throw new AppException("Một chương trình thay đổi giá khác đã tồn tại trong khoản thời gian từ " + startDate + " đến " + endDate + " vui lòng chọn khoản thời gian khác", HttpStatus.BAD_REQUEST);
        }
        //nếu có 1 priceheader khác có trạng thái đang hoạt động thì không thể tạo mới

        // Map the priceHeaderDto to a PriceHeader entity
        PriceHeader priceHeader = modelMapper.map(priceHeaderDto, PriceHeader.class);

        // Generate a random code for the priceHeader
        priceHeader.setCode(randomCode());
        priceHeader.setCreatedDate(LocalDateTime.now());
        priceHeader.setStatus(false);
        // Save the priceHeader to the database
        priceHeaderRepository.save(priceHeader);
    }

    //TODO: khi tạo thì mặc định status = false nhưng nếu ngày bắt đâu >= ngày hiện tại thì status = true
    //tự động cập nhật status khi ngày bắt đầu >= ngày hiện tại
//    @Async - số lượng khuyến mãi không nhiều nên không cần chạy đồng thời
    @Scheduled(fixedRate = 60000) // This will run the method every minute
    public void activePriceHeader() {
        LocalDateTime currentTime = LocalDateTime.now();

        priceHeaderRepository.updatePriceHeadersStatus(currentTime);
        priceHeaderRepository.updatePriceDetailsStatus(currentTime);
    }

    //TODO: update không cập nhật ngày bắt đầu
    @Override
    public void updatePriceHeader(PriceHeaderDto priceHeaderDto) {
// Parse the start date and end date from the priceHeaderDto
        LocalDateTime endDate = priceHeaderDto.getEndDate();


        // Find the sale price by its ID
        PriceHeader priceHeader = priceHeaderRepository.findById(priceHeaderDto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với mã là: " + priceHeaderDto.getId(), HttpStatus.NOT_FOUND));

        // Update the fields of the existing sale price with the new values
        if (!priceHeaderDto.getName().isEmpty() && !priceHeaderDto.getName().isBlank() && !priceHeaderDto.getName().equals(priceHeader.getName())) {
            priceHeader.setName(priceHeaderDto.getName());
        } else {
            priceHeader.setName(priceHeader.getName());
        }
        if (!priceHeaderDto.getDescription().isEmpty() && !priceHeaderDto.getDescription().isBlank() && !priceHeaderDto.getDescription().equals(priceHeader.getDescription())) {
            priceHeader.setDescription(priceHeaderDto.getDescription());
        } else {
            priceHeader.setDescription(priceHeader.getDescription());
        }
        //nếu chưa bắt đầu:
        if (priceHeader.getStartDate().isAfter(LocalDateTime.now())) {
            if (priceHeaderDto.getStartDate() != null && !priceHeaderDto.getStartDate().equals(priceHeader.getStartDate())) {
                priceHeader.setStartDate(priceHeaderDto.getStartDate());
            } else {
                priceHeader.setStartDate(priceHeader.getStartDate());

            }
        }
        if (priceHeaderDto.getEndDate() != null && !priceHeaderDto.getEndDate().equals(priceHeader.getEndDate())) {
            // Check if the end date is after the start date
            if (!endDate.isAfter(priceHeader.getStartDate())) {
                throw new AppException("Ngày kết thúc phải sau ngày hiện tại", HttpStatus.BAD_REQUEST);
            }
            if (endDate.isBefore(LocalDateTime.now())) {
                throw new AppException("Ngày kết thúc phải sau ngày hiện tại", HttpStatus.BAD_REQUEST);
            }
            // Check if the time period is already occupied
            boolean exists = priceHeaderRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, priceHeaderDto.getStartDate());
            if (exists) {
                throw new AppException("Một chương trình thay đổi giá khác đã tồn tại trong khoản thời gian từ " + priceHeaderDto.getStartDate() + " đến " + endDate + " vui lòng chọn khoản thời gian khác.", HttpStatus.BAD_REQUEST);
            }
            priceHeader.setEndDate(priceHeaderDto.getEndDate());
        } else {
            priceHeader.setEndDate(priceHeader.getEndDate());
        }
        if (priceHeaderDto.isStatus() != priceHeader.isStatus()) {
            if (priceHeaderDto.isStatus()) {
                boolean existsStatus = priceHeaderRepository.existsByStatus(true);
                if (existsStatus) {
                    throw new AppException("Không thể kích hoạt chương trình thay đổi giá khi đã có chương trình đang hoạt động", HttpStatus.BAD_REQUEST);
                }
            }
            priceHeader.setStatus(priceHeaderDto.isStatus());
            if (!priceHeader.isStatus()) {
                for (PriceDetail priceDetail : priceHeader.getPriceDetails()) {
                    priceDetail.setStatus(false);

                }

            }
        } else {
            priceHeader.setStatus(priceHeader.isStatus());
        }
        // Save the updated sale price to the database
        priceHeaderRepository.save(priceHeader);
        //khi tắt chương trình thì tắt tất cả các chi tiết giá


    }


    @Override
    public PriceHeaderDto getPriceHeaderById(Long id) {
        PriceHeader priceHeader = priceHeaderRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với mã là: " + id, HttpStatus.NOT_FOUND));
        PriceHeaderDto priceHeaderDto = modelMapper.map(priceHeader, PriceHeaderDto.class);
        priceHeaderDto.setPriceDetails(Collections.emptySet());
        return priceHeaderDto;
    }


    @Override
    public PriceHeader findById(Long id) {
        return priceHeaderRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với mã là: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void deletePriceHeaderById(Long id) {
        PriceHeader priceHeader = priceHeaderRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với mã là: " + id, HttpStatus.NOT_FOUND));
        if (priceHeader.isStatus()) {
            throw new AppException("Không thể xóa chương trình thay đổi giá đang hoạt động", HttpStatus.BAD_REQUEST);
        } else {
            priceHeaderRepository.delete(priceHeader);
        }

    }

    @Override
    public List<PriceHeaderDto> getAllPriceHeader(Integer page, Integer size, String code, String name, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PriceHeader> pageSalePrice;
        if (code != null && !code.isEmpty()) {
            pageSalePrice = priceHeaderRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            pageSalePrice = priceHeaderRepository.findByNameContaining(name, pageable);

        } else if (startDate != null && endDate != null) {
            //sort
            pageSalePrice = priceHeaderRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate, pageable);
        } else {
            pageSalePrice = priceHeaderRepository.findAllByOrderByCreatedDateDesc(pageable);
        }
        //sort by  created date
        return pageSalePrice.stream().sorted(Comparator.comparing(PriceHeader::getCreatedDate).reversed())
                .map(priceHeader ->
                {
                    PriceHeaderDto priceHeaderDto = modelMapper.map(priceHeader, PriceHeaderDto.class);
                    priceHeaderDto.setPriceDetails(Collections.emptySet());
                    return priceHeaderDto;
                }).toList();
    }

    @Override
    public long countAllPriceHeader(String code, String name, LocalDateTime startDate, LocalDateTime endDate) {
        if (code != null && !code.isEmpty()) {
            return priceHeaderRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return priceHeaderRepository.countByNameContaining(name);

        } else if (startDate != null && endDate != null) {
            return priceHeaderRepository.countByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate);
        } else {
            return priceHeaderRepository.count();
        }

    }

}
/*
 * Tôi có phương thức thêm chương trình giảm giá sau đây và tôi muốn thêm điều kiện, Tôi muốn tạo giảm giá với điều kiện là nếu khoảng thời gian từ startDate đến endDate không tồn tại (không thể trùng lặp), thời gian bắt đầu không được nhỏ hơn thời gian hiện tại(trong quá khứ),thời gian kết thúc phải sau thời gian bắt đầu:*/