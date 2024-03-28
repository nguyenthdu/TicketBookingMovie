package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.TicketDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.SalePriceDetail;
import com.app.TicketBookingMovie.models.Seat;
import com.app.TicketBookingMovie.models.ShowTime;
import com.app.TicketBookingMovie.models.Ticket;
import com.app.TicketBookingMovie.repository.SalePriceDetailRepository;
import com.app.TicketBookingMovie.repository.SeatRepository;
import com.app.TicketBookingMovie.repository.ShowTimeRepository;
import com.app.TicketBookingMovie.repository.TicketRepository;
import com.app.TicketBookingMovie.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ShowTimeRepository showTimeRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private SalePriceDetailRepository salePriceDetailRepository;

    @Transactional // Đảm bảo giao dịch tính nguyên vẹn
    @Override
    public void createTickets(TicketDto ticketDto) throws AppException {

        // Bước 1: Lấy thông tin

        Long showTimeId = ticketDto.getShowTimeId();
        Set<Long> seatIds = ticketDto.getSeatIds();

        // Bước 2: Kiểm tra tính hợp lệ của request

        if (showTimeId == null || showTimeId <= 0) {
            throw new AppException("Showtime id is invalid", HttpStatus.BAD_REQUEST);
        }

        if (seatIds == null || seatIds.isEmpty()) {
            throw new AppException("Seat list is empty", HttpStatus.BAD_REQUEST);
        }

        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new AppException("Showtime not found", HttpStatus.NOT_FOUND));

        if (!showTime.isStatus()) {
            throw new AppException("Showtime is not available", HttpStatus.BAD_REQUEST);
        }

        List<Seat> seats = seatRepository.findAllById(seatIds);
        //nếu ghế đó có trong danh sách ghế của showtime thì mới được đặt{
          for (Seat seat : seats) {
                if (!showTime.getSeats().contains(seat)) {
                 throw new AppException("Seat " + seat.getCode() + " is not in the room", HttpStatus.BAD_REQUEST);
                }
          }

        for (Seat seat : seats) {
            if (!seat.isStatus()) {
                throw new AppException("Seat " + seat.getCode() + " is not available", HttpStatus.BAD_REQUEST);
            }
        }

        int totalSeatsBooked = showTime.getSeatsBooked() + seats.size();
        if (totalSeatsBooked > showTime.getRoom().getTotalSeats()) {
            throw new AppException("Exceed the maximum number of seats", HttpStatus.BAD_REQUEST);
        }

        // Bước 3: Tìm chiến lược khuyến mãi
        LocalDateTime currentTime = LocalDateTime.now();
        List<SalePriceDetail> currentSalePriceDetails = salePriceDetailRepository.findCurrentSalePriceDetails(currentTime);

        // Bước 4: Xử lý vé trong vòng lặp

        List<Ticket> createdTickets = new ArrayList<>();
        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setCode(generateUniqueCode());
            ticket.setShowTime(showTime);
            ticket.setSeat(seat);

            // Định giá:
            Optional<SalePriceDetail> saleDetail = currentSalePriceDetails.stream()
                    .filter(s -> s.getTypeSeat().getId().equals(seat.getSeatType().getId()))
                    .findFirst();

            if (saleDetail.isPresent()) {
                ticket.setPrice(saleDetail.get().getPriceDecrease());
            } else {
                ticket.setPrice(seat.getSeatType().getPrice());
            }

            ticketRepository.save(ticket);
            createdTickets.add(ticket);
        }

        // Bước 5: Cập nhật lịch chiếu

        showTime.setSeatsBooked(totalSeatsBooked);
        showTimeRepository.save(showTime);

        // Cập nhật trạng thái ghế
        for (Seat seat : seats) {
            seat.setStatus(false);
            seatRepository.save(seat);
        }


    }

    private String generateUniqueCode() {
        // TODO: Implement logic to generate unique code for ticket
        return "VE"+LocalDateTime.now().getNano();
    }
}
