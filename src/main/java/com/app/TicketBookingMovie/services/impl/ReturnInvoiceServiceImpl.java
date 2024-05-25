package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.repository.ReturnInvoiceRepository;
import com.app.TicketBookingMovie.services.FoodService;
import com.app.TicketBookingMovie.services.InvoiceService;
import com.app.TicketBookingMovie.services.ReturnInvoviceService;
import com.app.TicketBookingMovie.services.ShowTimeService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReturnInvoiceServiceImpl implements ReturnInvoviceService {
    private final ReturnInvoiceRepository returnInvoiceRepository;
    private final InvoiceService invoiceService;
    private final ModelMapper modelMapper;
    private final FoodService foodService;
    private final ShowTimeService showTimeService;

    public ReturnInvoiceServiceImpl(ReturnInvoiceRepository returnInvoiceRepository, InvoiceService invoiceService,
            ModelMapper modelMapper, FoodService foodService, ShowTimeService showTimeService) {
        this.returnInvoiceRepository = returnInvoiceRepository;
        this.invoiceService = invoiceService;
        this.modelMapper = modelMapper;
        this.foodService = foodService;
        this.showTimeService = showTimeService;
    }

    private String randomCode() {
        return "HU" + LocalDateTime.now().getNano();
    }

    @Override
    @Transactional
    public void cancelInvoice(ReturnInvoiceDto returnInvoiceDto) {
        Long invoiceId = returnInvoiceDto.getInvoiceId();

        // Kiểm tra xem invoiceId có hợp lệ hay không
        if (invoiceId == null) {
            throw new IllegalArgumentException("invoiceId không được null");
        }

        Invoice invoice = invoiceService.findById(invoiceId);
        if (!invoice.isStatus()) {
            throw new AppException("Hóa đơn đã hủy trước đó", HttpStatus.BAD_REQUEST);
        }
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDate showDate = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowDate();
        LocalTime showTime = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowTime();
        LocalDateTime showTimeStart = LocalDateTime.of(showDate, showTime);

        // Kiểm tra xem lịch chiếu đã bắt đầu hay chưa
        if (currentTime.isBefore(showTimeStart)) {
            // Tính khoảng thời gian giữa thời điểm hiện tại và thời gian bắt đầu lịch chiếu
            Duration duration = Duration.between(currentTime, showTimeStart);
            long hoursUntilShowTime = duration.toHours();

            // Kiểm tra xem thời gian hủy vé có hợp lệ hay không (trước 2 giờ trước khi bắt
            // đầu)
            if (hoursUntilShowTime >= 2) {
                // Hoàn trả số lượng đồ ăn và slot của ghế trong lịch chiếu đã đặt
                refundFoodAndSeats(invoice);
                // Cập nhật trạng thái của hóa đơn thành false
                // Lưu thông tin hủy hóa đơn
                ReturnInvoice returnInvoice = modelMapper.map(returnInvoiceDto, ReturnInvoice.class);
                returnInvoice.setInvoice(invoice);
                returnInvoice.setCode(randomCode());
                returnInvoice.setReason(returnInvoiceDto.getReason());
                returnInvoice.setCancelDate(LocalDateTime.now());

                returnInvoiceRepository.save(returnInvoice);
                // Lưu cập nhật hóa đơn
                invoiceService.updateStatusInvoice(invoiceId, false);
                // xóa hóa đơn khỏi chương trình khuyến mãi đã áp dụng
                deletePromotionLine(invoice);
                // lấy sanh sách ghế từ invoice
                Set<Long> seats = invoice.getInvoiceTicketDetails().stream()
                        .map(invoiceTicketDetail -> invoiceTicketDetail.getTicket().getSeat().getId())
                        .collect(Collectors.toSet());
                // lấy id lịch chiếu từ invoice
                Long showTimeId = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getId();
                showTimeService.updateStatusHoldSeat(seats, showTimeId, false);
            } else {
                throw new AppException("Không thể hủy hóa đơn vì đã ít hơn 2 giờ trước khi bắt đầu lịch chiếu",
                        HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new AppException("Không thể hủy hóa đơn vì lịch chiếu đã bắt đầu", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ReturnInvoiceDto getReturnInvoice(Long invoiceId) {
        ReturnInvoice returnInvoice = returnInvoiceRepository.findByInvoiceId(invoiceId);
        if (returnInvoice == null) {
            throw new AppException("Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND);
        }
        ReturnInvoiceDto returnInvoiceDto = new ReturnInvoiceDto();
        returnInvoiceDto.setCode(returnInvoice.getCode());
        returnInvoiceDto.setReason(returnInvoice.getReason());
        returnInvoiceDto.setCancelDate(returnInvoice.getCancelDate());
        returnInvoiceDto.setInvoiceCode(returnInvoice.getInvoice().getCode());
        returnInvoiceDto.setInvoiceDate(returnInvoice.getInvoice().getCreatedDate());
        returnInvoiceDto.setUserCode(returnInvoice.getInvoice().getUser().getCode());
        returnInvoiceDto.setUserName(returnInvoice.getInvoice().getUser().getUsername());
        returnInvoiceDto.setTotal(returnInvoice.getInvoice().getTotalPrice());
        return returnInvoiceDto;
    }

    @Override
    public List<ReturnInvoiceDto> getAllReturnInvoice(Integer page, Integer size, String code, String userCode,
            LocalDate startDate, LocalDate endDate) {
        List<ReturnInvoice> pageReturnInvoice = returnInvoiceRepository
                .findAll(Sort.by(Sort.Direction.DESC, "cancelDate"));
        if (!code.isEmpty() && !code.isBlank()) {
            pageReturnInvoice = pageReturnInvoice.stream().filter(returnInvoice -> returnInvoice.getCode().equals(code))
                    .collect(Collectors.toList());
        } else if (!userCode.isEmpty() && !userCode.isBlank()) {
            pageReturnInvoice = pageReturnInvoice.stream()
                    .filter(returnInvoice -> returnInvoice.getInvoice().getUser().getCode().equals(userCode))
                    .collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pageReturnInvoice = pageReturnInvoice.stream()
                    .filter(returnInvoice -> returnInvoice.getCancelDate().isAfter(startDate.atStartOfDay())
                            && returnInvoice.getCancelDate().isBefore(endDate.atStartOfDay().plusDays(1)))
                    .collect(Collectors.toList());
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, pageReturnInvoice.size());
        return pageReturnInvoice.subList(fromIndex, toIndex).stream().map(returnInvoice -> {
            ReturnInvoiceDto returnInvoiceDto = new ReturnInvoiceDto();
            returnInvoiceDto.setCode(returnInvoice.getCode());
            returnInvoiceDto.setReason(returnInvoice.getReason());
            returnInvoiceDto.setCancelDate(returnInvoice.getCancelDate());
            returnInvoiceDto.setInvoiceId(returnInvoice.getInvoice().getId());
            returnInvoiceDto.setInvoiceCode(returnInvoice.getInvoice().getCode());
            returnInvoiceDto.setInvoiceDate(returnInvoice.getInvoice().getCreatedDate());
            returnInvoiceDto.setUserCode(returnInvoice.getInvoice().getUser().getCode());
            returnInvoiceDto.setUserName(returnInvoice.getInvoice().getUser().getUsername());
            returnInvoiceDto.setQuantity(returnInvoice.getInvoice().getInvoiceTicketDetails().size()
                    + returnInvoice.getInvoice().getInvoiceFoodDetails().size());
            returnInvoiceDto.setTotal(returnInvoice.getInvoice().getTotalPrice());
            return returnInvoiceDto;
        }).collect(Collectors.toList());
    }

    @Override
    public long countAllReturnInvoice(String code, String userCode, LocalDate startDate, LocalDate endDate) {
        if (!code.isEmpty() && !code.isBlank()) {
            return returnInvoiceRepository.countByCode(code);
        } else if (userCode != null) {
            return returnInvoiceRepository.countByUserCode(userCode);
        } else if (startDate != null && endDate != null) {
            return returnInvoiceRepository.countByCancelDateBetween(startDate.atStartOfDay(),
                    endDate.atStartOfDay().plusDays(1));
        } else {
            return returnInvoiceRepository.count();
        }
    }

    private void refundFoodAndSeats(Invoice invoice) {
        // Hoàn trả số lượng đồ ăn
        List<InvoiceFoodDetail> foodDetails = invoice.getInvoiceFoodDetails();
        for (InvoiceFoodDetail foodDetail : foodDetails) {
            foodService.updateQuantityFood(foodDetail.getFood().getId(), foodDetail.getFood().getCinema().getId(),
                    foodDetail.getQuantity());
        }

        // Lấy danh sách ghế đã đặt trong lịch chiếu
        Set<Seat> seats = invoice.getInvoiceTicketDetails().stream()
                .map(invoiceTicketDetail -> invoiceTicketDetail.getTicket().getSeat()).collect(Collectors.toSet());
        // lấy danh sách ghế đã đặt trong lịch chiếu
        List<ShowTimeSeat> showTimeSeats = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime()
                .getShowTimeSeat().stream().filter(showTimeSeat -> seats.contains(showTimeSeat.getSeat()))
                .collect(Collectors.toList());

        // Cập nhật trạng thái của ghế
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            showTimeSeat.setStatus(true);
            // cập nhập lại số lượng ghế đã đặt
            ShowTime showTime = showTimeSeat.getShowTime();
            showTime.setSeatsBooked(showTime.getSeatsBooked() - 1);
        }

        // Cập nhật trạng thái của ghế trong lịch chiếu
        ShowTime showTime = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime();
        showTimeService.updateSeatStatus(showTime);
    }

    // khi hủy thành công thì xóa hóa đơn khỏi chương trình khuyến mãi đã áp dung
    public void deletePromotionLine(Invoice invoice) {
        // kiểm tra xem hóa đơn có chương trình khuyến mãi nào không, nếu có thì mới gọi
        // phương thức xóa
        if (!invoice.getPromotionLines().isEmpty())
            invoiceService.removePromotionLineFromInvoice(invoice.getId(),
                    invoice.getPromotionLines().stream().findFirst().get().getId());
    }
}
