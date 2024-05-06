package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceHeaderDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.repository.PriceHeaderRepository;
import com.app.TicketBookingMovie.services.PriceHeaderService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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


    @Async
    @Scheduled(cron = "0 * * * * ?") // Run every minute
    @Transactional
    public void updateStatusPrice() {
        LocalDateTime now = LocalDateTime.now();
        priceHeaderRepository.findAll().forEach(priceHeader -> {
            if (priceHeader.getEndDate().isBefore(now)) {
                priceHeader.setStatus(false);
                priceHeader.getPriceDetails().forEach(priceDetail -> {
                    priceDetail.setStatus(false);
                });
            }
            priceHeaderRepository.save(priceHeader);
        });

    }

    //TODO: update không cập nhật ngày bắt đầu
    @Override
    public void updatePriceHeader(PriceHeaderDto priceHeaderDto) {
        // Parse the start date and end date from the priceHeaderDto
        LocalDateTime endDate = priceHeaderDto.getEndDate();
        LocalDateTime startDate = priceHeaderDto.getStartDate();
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
        if (priceHeaderDto.getStartDate() != null) {
            //nếu như ngày bắt đầu đã qua ngày hiện tại thì không thể cập nhật
            if (priceHeaderDto.getStartDate().isBefore(LocalDateTime.now()) || priceHeaderDto.getStartDate().getDayOfMonth() == LocalDate.now().getDayOfMonth()) {
                throw new AppException("Không thể cập nhật ngày bắt đầu đã qua ngày hiện tại!!!", HttpStatus.BAD_REQUEST);
            }
            // kiểm tra xem ngày bắt đầu có nằm trong 1 khoản thời gian của 1 price header khác không
            boolean exists = priceHeaderRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(priceHeader.getEndDate(), priceHeaderDto.getStartDate());
            if (exists) {
                throw new AppException("Ngày bắt đầu đã nằm trong khoản thời gian của 1 chương trình thay đổi giá khác", HttpStatus.BAD_REQUEST);
            }
            priceHeader.setStartDate(priceHeaderDto.getStartDate());

        } else {
            priceHeader.setStartDate(priceHeader.getStartDate());
        }
        if (priceHeaderDto.getEndDate() != null && !priceHeaderDto.getEndDate().equals(priceHeader.getEndDate())) {
            // Check if the end date is after the start date
            if (!endDate.isAfter(priceHeader.getStartDate())) {
                throw new AppException("Ngày kết thúc phải sau ngày hiện tại", HttpStatus.BAD_REQUEST);
            }
            if (endDate.isBefore(LocalDateTime.now())) {
                throw new AppException("Ngày kết thúc phải sau ngày hiện tại!!!", HttpStatus.BAD_REQUEST);
            }
            // Check if the time period is already occupied
            boolean exists = priceHeaderRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(priceHeader.getStartDate(), priceHeaderDto.getEndDate());
            if (exists) {
                throw new AppException("Ngày kết thúc đã nằm trong khoản thời gian của 1 chương trình thay đổi giá khác!!!", HttpStatus.BAD_REQUEST);
            }
            priceHeader.setEndDate(priceHeaderDto.getEndDate());
        } else {
            priceHeader.setEndDate(priceHeader.getEndDate());
        }
        if (priceHeaderDto.isStatus() != priceHeader.isStatus()) {
            if (priceHeaderDto.isStatus()) {
                boolean existsStatus = priceHeaderRepository.existsByStatus(true);
                if (existsStatus) {
                    throw new AppException("Không thể kích hoạt chương trình thay đổi giá khi đã có chương trình đang hoạt động!!!", HttpStatus.BAD_REQUEST);
                }
                //nếu chưa tới ngày bắt đầu thì không thể kích hoạt
                if (priceHeader.getStartDate().isAfter(LocalDateTime.now())) {
                    throw new AppException("Chương trình thay đổi giá chưa tới ngày bắt đầu, không thể kích hoạt!!!", HttpStatus.BAD_REQUEST);
                }
            }
            priceHeader.setStatus(priceHeaderDto.isStatus());
        } else {
            priceHeader.setStatus(priceHeader.isStatus());
        }
        // Save the updated sale price to the database
        priceHeaderRepository.save(priceHeader);
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
    public List<PriceHeaderDto> getAllPriceHeader(Integer page, Integer size, String code, String name, LocalDate startDate, LocalDate endDate) {

        List<PriceHeader> pageSalePrice = priceHeaderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if (code != null && !code.isEmpty()) {
            pageSalePrice = pageSalePrice.stream().filter(priceHeader -> priceHeader.getCode().equals(code)).toList();
        } else if (name != null && !name.isEmpty()) {
            pageSalePrice = pageSalePrice.stream().filter(priceHeader -> priceHeader.getName().contains(name)).toList();
        } else if (startDate != null && endDate != null) {
            pageSalePrice = pageSalePrice.stream().filter(priceHeader -> priceHeader.getStartDate().isAfter(startDate.atStartOfDay())
                    && priceHeader.getEndDate().isBefore(endDate.atStartOfDay().plusDays(1))).toList();
        }
        int start = page * size;
        int end = Math.min((start + size), pageSalePrice.size());
        List<PriceHeaderDto> priceHeaderDtos = pageSalePrice.subList(start, end).stream().map(priceHeader -> modelMapper.map(priceHeader, PriceHeaderDto.class)).toList();
        priceHeaderDtos.forEach(priceHeaderDto -> priceHeaderDto.setPriceDetails(Collections.emptySet()));
        return priceHeaderDtos;
    }

    @Override
    public long countAllPriceHeader(String code, String name, LocalDate startDate, LocalDate endDate) {
        if (code != null && !code.isEmpty()) {
            return priceHeaderRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return priceHeaderRepository.countByNameContaining(name);

        } else if (startDate != null && endDate != null) {
            return priceHeaderRepository.countByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate.atStartOfDay(), endDate.atStartOfDay());
        } else {
            return priceHeaderRepository.count();
        }
    }
}