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
    private  final FoodService foodService;
    private final ShowTimeService showTimeService;

    public ReturnInvoiceServiceImpl(ReturnInvoiceRepository returnInvoiceRepository, InvoiceService invoiceService, ModelMapper modelMapper, FoodService foodService, ShowTimeService showTimeService) {
        this.returnInvoiceRepository = returnInvoiceRepository;
        this.invoiceService = invoiceService;
        this.modelMapper = modelMapper;
        this.foodService = foodService;
        this.showTimeService = showTimeService;
    }

    private String randomCode() {
        return "HU"+ LocalDateTime.now().getNano();
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
        if(!invoice.isStatus()){
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

            // Kiểm tra xem thời gian hủy vé có hợp lệ hay không (trước 2 giờ trước khi bắt đầu)
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
            } else {
                throw new AppException("Không thể hủy hóa đơn vì đã ít hơn 2 giờ trước khi bắt đầu lịch chiếu", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new AppException("Không thể hủy hóa đơn vì lịch chiếu đã bắt đầu", HttpStatus.BAD_REQUEST);
        }
    }

    private void refundFoodAndSeats(Invoice invoice) {
        // Hoàn trả số lượng đồ ăn
        List<InvoiceFoodDetail> foodDetails = invoice.getInvoiceFoodDetails();
        for (InvoiceFoodDetail foodDetail : foodDetails) {
            foodService.updateQuantityFood(foodDetail.getFood().getId(), foodDetail.getFood().getCinema().getId(), foodDetail.getQuantity());
        }

        // Lấy danh sách ghế đã đặt trong lịch chiếu
        Set<Seat> seats = invoice.getInvoiceTicketDetails().stream().map(invoiceTicketDetail -> invoiceTicketDetail.getTicket().getSeat()).collect(Collectors.toSet());
        //lấy danh sách ghế đã đặt trong lịch chiếu
        List<ShowTimeSeat> showTimeSeats =   invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowTimeSeat().stream().filter(showTimeSeat -> seats.contains(showTimeSeat.getSeat())).collect(Collectors.toList());

// Cập nhật trạng thái của ghế
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            showTimeSeat.setStatus(true);
            //cập nhập lại số lượng ghế đã đặt
            ShowTime showTime = showTimeSeat.getShowTime();
            showTime.setSeatsBooked(showTime.getSeatsBooked() - 1);
        }

        // Cập nhật trạng thái của ghế trong lịch chiếu
        ShowTime showTime = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime();
        showTimeService.updateSeatStatus(showTime);
    }
}
