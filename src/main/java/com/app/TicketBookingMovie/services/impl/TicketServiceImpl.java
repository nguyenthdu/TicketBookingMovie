package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.TicketDto;
import com.app.TicketBookingMovie.repository.SeatRepository;
import com.app.TicketBookingMovie.repository.ShowTimeRepository;
import com.app.TicketBookingMovie.repository.TicketRepository;
import com.app.TicketBookingMovie.services.TicketService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServiceImpl implements TicketService {

    private final ShowTimeRepository showTimeRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final ModelMapper modelMapper;

    public TicketServiceImpl(ShowTimeRepository showTimeRepository, SeatRepository seatRepository, TicketRepository ticketRepository, ModelMapper modelMapper) {
        this.showTimeRepository = showTimeRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void createTicket(TicketDto ticketDto) {
//        // Lấy thông tin về lịch chiếu từ ID được cung cấp trong ticketDto
//        ShowTime showTime = showTimeRepository.findById(ticketDto.getShowTimeId())
//                .orElseThrow(() -> new AppException("ShowTime not found with id: " + ticketDto.getShowTimeId(), HttpStatus.NOT_FOUND));
//
//        // Kiểm tra xem lịch chiếu có còn vé trống không
//        int availableSeats = showTime.getRoom().getTotalSeats() - showTime.getSeatsBooked();
//        if (availableSeats <= 0) {
//            throw new AppException("There are no available seats for this showtime.", HttpStatus.BAD_REQUEST);
//        }
//
//        // Kiểm tra số lượng ghế được đặt không vượt quá số lượng ghế trống
//        List<Long> seatIds = ticketDto.getSeatIds();
//        if (seatIds.size() > availableSeats) {
//            throw new AppException("The number of tickets exceeds the available seats.", HttpStatus.BAD_REQUEST);
//        }
//
//        // Lấy thông tin về các ghế từ danh sách ID được cung cấp trong ticketDto
//        List<Seat> seats = seatRepository.findAllByIdIn(seatIds);
//
//        // Kiểm tra xem tất cả các ghế có tồn tại và có thể đặt vé được không
//        if (seats.size() != seatIds.size()) {
//            throw new AppException("One or more selected seats do not exist.", HttpStatus.BAD_REQUEST);
//        }
//
//        // Kiểm tra xem tất cả các ghế đã chọn có còn trống không
//        for (Seat seat : seats) {
//            if (!seat.isStatus()) {
//                throw new AppException("Seat with ID " + seat.getId() + " is already booked.", HttpStatus.BAD_REQUEST);
//            }
//        }
//
//        // Kiểm tra xem thời gian hiện tại có nằm trong thời gian khuyến mãi không
//        boolean isPromotionTime = false;
//        LocalDateTime currentTime = LocalDateTime.now();
//        for (SalePriceDetail salePriceDetail : showTime.getRoom().getSalePrice().getSalePriceDetails()) {
//            if (salePriceDetail.getTypeSeat() != null && seatIds.contains(salePriceDetail.getTypeSeat().getId())) {
//                LocalDateTime startDate = salePriceDetail.getSalePrice().getStartDate();
//                LocalDateTime endDate = salePriceDetail.getSalePrice().getEndDate();
//                if (currentTime.isAfter(startDate) && currentTime.isBefore(endDate)) {
//                    isPromotionTime = true;
//                    break;
//                }
//            }
//        }
//
//        // Tính toán giá vé dựa trên thời gian khuyến mãi
//        double totalPrice = 0.0;
//        if (isPromotionTime) {
//            for (SalePriceDetail salePriceDetail : showTime.getRoom().getSalePrice().getSalePriceDetails()) {
//                if (salePriceDetail.getTypeSeat() != null && seatIds.contains(salePriceDetail.getTypeSeat().getId())) {
//                    totalPrice += salePriceDetail.getPriceDecrease();
//                }
//            }
//        } else {
//            // Nếu không trong thời gian khuyến mãi, sử dụng giá vé mặc định của từng ghế
//            for (Seat seat : seats) {
//                totalPrice += seat.getPrice();
//            }
//        }
//
//        // Tạo và lưu vé vào cơ sở dữ liệu
//        for (Seat seat : seats) {
//            Ticket ticket = new Ticket();
//            ticket.setCode(randomCode());
//            ticket.setShowTime(showTime);
//            ticket.setSeat(seat);
//            ticket.setPrice(totalPrice / seatIds.size()); // Chia đều giá vé cho số lượng ghế
//            ticketRepository.save(ticket);
//
//            // Cập nhật trạng thái của ghế đã đặt và tăng số lượng ghế đã đặt
//            seat.setStatus(false);
//            showTime.setSeatsBooked(showTime.getSeatsBooked() + 1);
//        }
//
//        // Lưu các thay đổi vào cơ sở dữ liệu
//        showTimeRepository.save(showTime);
//        seatRepository.saveAll(seats);
    }

    // Phương thức sinh mã ngẫu nhiên cho vé
    private String randomCode() {
        // Logic sinh mã ngẫu nhiên ở đây
        return ""; // Cần cài đặt logic sinh mã ngẫu nhiên
    }
}
