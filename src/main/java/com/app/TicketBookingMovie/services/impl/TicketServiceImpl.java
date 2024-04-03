package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Seat;
import com.app.TicketBookingMovie.models.ShowTime;
import com.app.TicketBookingMovie.models.ShowTimeSeat;
import com.app.TicketBookingMovie.models.Ticket;
import com.app.TicketBookingMovie.repository.PriceDetailRepository;
import com.app.TicketBookingMovie.repository.SeatRepository;
import com.app.TicketBookingMovie.repository.ShowTimeRepository;
import com.app.TicketBookingMovie.repository.TicketRepository;
import com.app.TicketBookingMovie.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepository;

    private final ShowTimeRepository showTimeRepository;

    private final SeatRepository seatRepository;

    private final PriceDetailRepository priceDetailRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, ShowTimeRepository showTimeRepository, SeatRepository seatRepository, PriceDetailRepository priceDetailRepository) {
        this.ticketRepository = ticketRepository;
        this.showTimeRepository = showTimeRepository;
        this.seatRepository = seatRepository;
        this.priceDetailRepository = priceDetailRepository;

    }

    @Transactional // Đảm bảo giao dịch tính nguyên vẹn
    @Override
    public List<Ticket> createTickets(Long showTimeId, Set<Long> seatIds) throws AppException {

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
        //Bước 3:kiểm tra xem danh sách id ghế nhập vào có tồn tại trong danh sách ghế của Showtim đó hay không, nếu có thì kiểm tra tiếp trạng thái của nó trong ShowTimeSeat
        Set<ShowTimeSeat> showTimeSeats = showTime.getShowTimeSeat();
        List<Seat> seats = new ArrayList<>();
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new AppException("Seat not found with id: " + seatId, HttpStatus.NOT_FOUND));

            boolean isExist = showTimeSeats.stream()
                    .anyMatch(showTimeSeat -> showTimeSeat.getSeat().getId().equals(seatId));

            if (!isExist) {
                throw new AppException("Seat " + seat.getCode() + " is not exist in showtime", HttpStatus.BAD_REQUEST);
            }

            seats.add(seat);
        }
        //Bước 4: kiểm tra trạng thái của ghế trong ShowTimeSeat
        for (Seat seat : seats) {
            boolean isBooked = showTimeSeats.stream()
                    .filter(showTimeSeat -> showTimeSeat.getSeat().getId().equals(seat.getId()))
                    .findFirst()
                    .get()
                    .isStatus();

            if (!isBooked) {
                throw new AppException("Seat " + seat.getCode() + " has been booked", HttpStatus.BAD_REQUEST);
            }
        }
        int totalSeatsBooked = showTime.getSeatsBooked() + seats.size();
        if (totalSeatsBooked > showTime.getRoom().getTotalSeats()) {
            throw new AppException("Exceed the maximum number of seats", HttpStatus.BAD_REQUEST);
        }


        // Bước 4: Xử lý vé tạo vé với mỗi ghế được thêm vào
        List<Ticket> createdTickets = new ArrayList<>();
        for (Seat seat : seats) {
            Ticket ticket = new Ticket();
            ticket.setCode(randomCode());
            ticket.setShowTime(showTime);
            ticket.setSeat(seat);
            ticketRepository.save(ticket);
            createdTickets.add(ticket);
        }

        // Bước 5: Cập nhật lịch chiếu
        showTime.setSeatsBooked(totalSeatsBooked);
        showTimeRepository.save(showTime);
        // Bước 6: Cập nhật trạng thái ghế trong lịch chiếu cụ thể
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            if (seats.stream().anyMatch(s -> s.getId().equals(showTimeSeat.getSeat().getId()))) {
                showTimeSeat.setStatus(false);
            }
        }
        return createdTickets;

    }

    private String randomCode() {
        // TODO: Implement logic to generate unique code for ticket
        return "VE" + LocalDateTime.now().getNano();
    }
}
