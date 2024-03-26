package com.app.TicketBookingMovie.services.impl;


import com.app.TicketBookingMovie.dtos.ShowTimeDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Movie;
import com.app.TicketBookingMovie.models.Room;
import com.app.TicketBookingMovie.models.ShowTime;
import com.app.TicketBookingMovie.repository.MovieRepository;
import com.app.TicketBookingMovie.repository.RoomRepository;
import com.app.TicketBookingMovie.repository.ShowTimeRepository;
import com.app.TicketBookingMovie.services.ShowTimeService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ShowTimeServiceImpl implements ShowTimeService {


    private final ShowTimeRepository showTimeRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    public ShowTimeServiceImpl(ShowTimeRepository showTimeRepository, RoomRepository roomRepository, MovieRepository movieRepository, ModelMapper modelMapper) {
        this.showTimeRepository = showTimeRepository;
        this.roomRepository = roomRepository;
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createShowTime(Set<ShowTimeDto> showTimeDtos) {
        Set<ShowTimeDto> createdShowTimes = new HashSet<>();

        LocalDate currentDate = LocalDate.now().plusDays(1); // Lấy ngày hiện tại và thêm một ngày


        for (ShowTimeDto showTimeDto : showTimeDtos) {
            Room room = roomRepository.findById(showTimeDto.getRoomId())
                    .orElseThrow(() -> new AppException("Room not found with id: " + showTimeDto.getRoomId(), HttpStatus.NOT_FOUND));
            Movie movie = movieRepository.findById(showTimeDto.getMovieId())
                    .orElseThrow(() -> new AppException("Movie not found with id: " + showTimeDto.getMovieId(), HttpStatus.NOT_FOUND));
            LocalDate showDate = showTimeDto.getShowDate();

            // Kiểm tra xem ngày chiếu có phải là sau ngày hiện tại không
            if (showDate.isBefore(currentDate)) {
                throw new AppException("Show date must be after the current date.", HttpStatus.BAD_REQUEST);
            }
            if (!movie.isStatus()) {
                throw new AppException("The movie is not available for scheduling.", HttpStatus.BAD_REQUEST);
            }
            if (!room.isStatus()) {
                throw new AppException("The room is not available for scheduling.", HttpStatus.BAD_REQUEST);
            }
            LocalTime newShowTimeStart = showTimeDto.getShowTime();
            // Convert duration to minutes
            int movieDuration = movie.getDurationMinutes();
            LocalTime newShowTimeEnd = newShowTimeStart.plusMinutes(movieDuration).plusHours(1);

            // Kiểm tra xem ngày chiếu có phải là sau ngày hiện tại không
            if (showDate.isBefore(currentDate)) {
                throw new AppException("Show date must be after the current date.", HttpStatus.BAD_REQUEST);
            }

            // Kiểm tra xem giờ chiếu mới có trùng với bất kỳ giờ chiếu nào khác trong cùng một ngày và phòng không.
            List<ShowTime> existingShowTimes = showTimeRepository.findByRoomAndShowDate(room, showDate);

            for (ShowTime existingShowTime : existingShowTimes) {
                LocalTime existingShowTimeStart = existingShowTime.getShowTime();
                LocalTime existingShowTimeEnd = existingShowTimeStart.plusMinutes(existingShowTime.getMovie().getDurationMinutes()).plusHours(1);

                if (newShowTimeStart.isBefore(existingShowTimeEnd) && newShowTimeEnd.isAfter(existingShowTimeStart)) {
                    throw new AppException("The new showtime overlaps with an existing showtime.", HttpStatus.BAD_REQUEST);
                }
            }

            // Kiểm tra xem thời gian bắt đầu của phim tiếp theo có cách thời gian kết thúc của phim trước đó ít nhất một giờ không.
            if (!existingShowTimes.isEmpty()) {
                ShowTime lastShowTime = existingShowTimes.get(existingShowTimes.size() - 1);
                LocalTime lastShowTimeEnd = lastShowTime.getShowTime().plusMinutes(lastShowTime.getMovie().getDurationMinutes()).plusHours(1);
                if (newShowTimeStart.isBefore(lastShowTimeEnd.plusHours(1))) {
                    throw new AppException("The new showtime must be at least 1 hour after the end time of the previous movie.", HttpStatus.BAD_REQUEST);
                }
            }

            // Tạo giờ chiếu mới
            ShowTime newShowTime = modelMapper.map(showTimeDto, ShowTime.class);
            newShowTime.setCode(randomCode());
            newShowTime.setRoom(room);
            newShowTime.setMovie(movie);
            newShowTime.setStatus(showTimeDto.isStatus());
            newShowTime.setSeatsBooked(0);
            newShowTime.setCreatedDate(LocalDateTime.now());
            showTimeRepository.save(newShowTime);
            createdShowTimes.add(modelMapper.map(newShowTime, ShowTimeDto.class));
        }


    }

    @Override
    public ShowTimeDto getShowTimeById(Long id) {
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Showtime not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(showTime, ShowTimeDto.class);
    }

    @Override
    public List<ShowTimeDto> getAllShowTimes(Integer page, Integer size, String code, Long movieId, LocalDate date) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShowTime> showTimes;
        if (code != null && !code.isEmpty()) {
            showTimes = showTimeRepository.findByCode(code, pageable);
        } else if (movieId != null && movieId > 0 && date != null && !date.toString().isEmpty()) {
            showTimes = showTimeRepository.findByMovieIdAndShowDate(movieId, date, pageable);
        } else {
            showTimes = showTimeRepository.findAll(pageable);
        }
        return showTimes.map(showTime -> modelMapper.map(showTime, ShowTimeDto.class)).getContent();
    }

    @Override
    public void updateShowTimeStatus() {
        LocalDate currentDate = LocalDate.now(); // Ngày hiện tại
        LocalTime currentTime = LocalTime.now(); // Thời gian hiện tại

        List<ShowTime> allShowTimes = showTimeRepository.findAll();

        for (ShowTime showTime : allShowTimes) {
            LocalDate showDate = showTime.getShowDate();
            LocalTime showTimeStart = showTime.getShowTime();
            boolean isShowTimePassed = showDate.isBefore(currentDate) || (showDate.isEqual(currentDate) && showTimeStart.isBefore(currentTime));
            boolean isSeatsExceeded = showTime.getSeatsBooked() >= showTime.getRoom().getTotalSeats();

            // Cập nhật trạng thái của lịch chiếu
            boolean status = !(isShowTimePassed || isSeatsExceeded);
            showTime.setStatus(status);

            // Lưu lại trạng thái đã cập nhật vào cơ sở dữ liệu
            showTimeRepository.save(showTime);
        }
    }

    @Override
    public long countAllShowTimes(String code, Long movieId, LocalDate date) {
        if (code != null && !code.isEmpty()) {
            return showTimeRepository.countByCodeContaining(code);
        } else if (movieId != null && movieId > 0 && date != null && !date.toString().isEmpty()) {
            return showTimeRepository.countByMovieIdAndShowDate(movieId,date);
        } else {
            return showTimeRepository.count();
        }
    }


    public String randomCode() {
        return  "LC" + LocalDateTime.now().getNano();
    }
}