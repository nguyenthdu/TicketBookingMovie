package com.app.TicketBookingMovie.services.impl;


import com.app.TicketBookingMovie.dtos.SeatDto;
import com.app.TicketBookingMovie.dtos.ShowTimeDto;
import com.app.TicketBookingMovie.dtos.ShowTimeSeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.repository.MovieRepository;
import com.app.TicketBookingMovie.repository.RoomRepository;
import com.app.TicketBookingMovie.repository.ShowTimeRepository;
import com.app.TicketBookingMovie.repository.ShowTimeSeatRepository;
import com.app.TicketBookingMovie.services.ShowTimeService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class ShowTimeServiceImpl implements ShowTimeService {


    private final ShowTimeRepository showTimeRepository;
    private final ShowTimeSeatRepository showTimeSeatRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    public ShowTimeServiceImpl(ShowTimeRepository showTimeRepository, ShowTimeSeatRepository showTimeSeatRepository, RoomRepository roomRepository, MovieRepository movieRepository, ModelMapper modelMapper) {
        this.showTimeRepository = showTimeRepository;
        this.showTimeSeatRepository = showTimeSeatRepository;
        this.roomRepository = roomRepository;
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional
    @Override
    public void createShowTime(Set<ShowTimeDto> showTimeDtos) {
        Set<ShowTimeDto> createdShowTimes = new HashSet<>();

        LocalDate currentDate = LocalDate.now().plusDays(1); // Lấy ngày hiện tại và thêm một ngày


        for (ShowTimeDto showTimeDto : showTimeDtos) {
            Room room = roomRepository.findById(showTimeDto.getRoomId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phòng chiếu với id: " + showTimeDto.getRoomId(), HttpStatus.NOT_FOUND));
            Movie movie = movieRepository.findById(showTimeDto.getMovieId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + showTimeDto.getMovieId(), HttpStatus.NOT_FOUND));
            LocalDate showDate = showTimeDto.getShowDate();

            // Kiểm tra xem ngày chiếu có phải là sau ngày hiện tại không
            if (showDate.isBefore(currentDate)) {
                throw new AppException("Ngày chiếu phải sau ngày hiện tại.", HttpStatus.BAD_REQUEST);
            }
            if (!movie.isStatus()) {
                throw new AppException("Trạng thái phim không hoạt động.", HttpStatus.BAD_REQUEST);
            }
            if (!room.isStatus()) {
                throw new AppException("Trạng thái phòng chiếu không hoạt động.", HttpStatus.BAD_REQUEST);
            }

            //TODO: xử lý khoảng thời gian giữa các lịch chiếu trong cùng 1 ngày cùng 1 phòng

            List<ShowTime> existingShowTimes = showTimeRepository.findByRoomAndShowDate(room, showDate);
            existingShowTimes.sort(Comparator.comparing(ShowTime::getShowTime));
            LocalTime newShowTimeStart = showTimeDto.getShowTime();
            LocalTime newShowTimeEnd = newShowTimeStart.plusMinutes(movie.getDurationMinutes()).plusMinutes(60);
            for (ShowTime existingShowTime : existingShowTimes) {
                LocalTime existingShowTimeStart = existingShowTime.getShowTime();
                LocalTime existingShowTimeEnd = existingShowTimeStart.plusMinutes(existingShowTime.getMovie().getDurationMinutes()).plusMinutes(60);
                if (newShowTimeStart.isBefore(existingShowTimeEnd) && newShowTimeEnd.isAfter(existingShowTimeStart)) {
                    throw new AppException("Không thể tạo lịch chiếu mới vì trùng với lịch chiếu đã có.", HttpStatus.BAD_REQUEST);
                }
            }


            //TODO: Tạo giờ chiếu mới
            ShowTime newShowTime = new ShowTime();
            newShowTime.setCode(randomCode());
            newShowTime.setShowDate(showDate);
            newShowTime.setShowTime(showTimeDto.getShowTime());
            newShowTime.setRoom(room);
            newShowTime.setMovie(movie);
            newShowTime.setStatus(showTimeDto.isStatus());
            newShowTime.setSeatsBooked(0);
            newShowTime.setCreatedDate(LocalDateTime.now());

            Set<Seat> seats = room.getSeats();
            Set<ShowTimeSeat> showTimeSeats = new HashSet<>();
            for (Seat seat : seats) {
                ShowTimeSeat showTimeSeat = new ShowTimeSeat();
                showTimeSeat.setSeat(seat);
                showTimeSeat.setShowTime(newShowTime);
                showTimeSeat.setStatus(true); // Khởi tạo trạng thái của ghế là true
                showTimeSeats.add(showTimeSeat);
            }
            newShowTime.setShowTimeSeat(showTimeSeats);
            showTimeRepository.save(newShowTime);
            createdShowTimes.add(modelMapper.map(newShowTime, ShowTimeDto.class));
        }



    }
    //sau 1 tuần sẽ dự động xóa các ShowTimeSeat của lịch chiếu đã qua 1 tuần
    @Async
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void deleteShowTimeSeatAsync() {
        LocalDate currentDate = LocalDate.now(); // Ngày hiện tại
        List<ShowTime> allShowTimes = showTimeRepository.findAll();
        for (ShowTime showTime : allShowTimes) {
            LocalDate showDate = showTime.getShowDate();
            boolean isShowTimePassed = showDate.isBefore(currentDate);
            if (isShowTimePassed) {
                Set<ShowTimeSeat> showTimeSeats = showTime.getShowTimeSeat();
                showTimeSeatRepository.deleteAll(showTimeSeats);
            }
        }
    }

    @Override
    public ShowTimeDto getShowTimeById(Long id) {
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy lịch chiếu với id: " + id, HttpStatus.NOT_FOUND));
        ShowTimeDto showTimeDto = modelMapper.map(showTime, ShowTimeDto.class);
        showTimeDto.setCinemaName(showTime.getRoom().getCinema().getName());
        return showTimeDto;
    }

    @Override
    public ShowTime findById(Long id) {
        return showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy lịch chiếu với id: " + id, HttpStatus.NOT_FOUND));
    }


    @Override
    public void updateShowTime(ShowTimeDto showTimeDto) {
        // Lấy thông tin lịch chiếu từ cơ sở dữ liệu
        ShowTime showTime = showTimeRepository.findById(showTimeDto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy lịch chiếu với id: " + showTimeDto.getId(), HttpStatus.NOT_FOUND));

        // Kiểm tra nếu thời gian lịch chiếu đã qua, không thể cập nhật
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDate showDate = showTime.getShowDate();
        LocalTime showTimeStart = showTime.getShowTime();
        boolean isShowTimePassed = showDate.isBefore(currentDate) || (showDate.isEqual(currentDate) && showTimeStart.isBefore(currentTime));
        if (isShowTimePassed) {
            throw new AppException("Không thể cập nhật lịch chiếu đã qua.", HttpStatus.BAD_REQUEST);
        }

        // Kiểm tra nếu có ghế đã được đặt, chỉ được cập nhật trạng thái
        if (showTime.getSeatsBooked() > 0) {
            showTime.setStatus(showTimeDto.isStatus());
        } else {
            // Kiểm tra ngày chiếu nếu chưa đến ngày hiện tại thì cập nhật được
            if (showTime.getShowDate().isAfter(currentDate) || showTime.getShowDate().isEqual(currentDate)) {
                showTime.setShowDate(showTimeDto.getShowDate());
                // Kiểm tra giờ chiếu nếu chưa đến thời gian hiện tại thì cập nhật được
                if (showTime.getShowTime().isAfter(currentTime) || showTime.getShowTime().equals(currentTime)) {
                    showTime.setShowTime(showTimeDto.getShowTime());
                }
                // Cập nhật phòng chiếu và phim
                showTime.setRoom(roomRepository.findById(showTimeDto.getRoomId())
                        .orElseThrow(() -> new AppException("Không tìm thấy phòng chiếu với id: " + showTimeDto.getRoomId(), HttpStatus.NOT_FOUND)));
                showTime.setMovie(movieRepository.findById(showTimeDto.getMovieId())
                        .orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + showTimeDto.getMovieId(), HttpStatus.NOT_FOUND)));
                showTime.setStatus(showTimeDto.isStatus());
            } else {
                throw new AppException("Không thể cập nhật lịch chiếu đang hoạt động.", HttpStatus.BAD_REQUEST);
            }
        }

        // Lưu lại cập nhật vào cơ sở dữ liệu
        showTimeRepository.save(showTime);
    }
    //kiểm tra lại danh sách ghế đã đặt



    //Xử lý nếu thời gian lịch chiếu đã qua hoặc số ghế đã đặt vượt quá số ghế của phòng chiếu thì trạng thái của lịch chiếu sẽ là false
    @Async
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    public void updateShowTimeStatusAsync() {
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
    public List<ShowTimeDto> getAllShowTimes(Integer page, Integer size, String code, Long cinemaId, Long movieId, LocalDate date, Long roomId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ShowTime> showTimes;

        if (code != null && !code.isEmpty()) {
            showTimes = showTimeRepository.findByCode(code, pageable);
        } else if (movieId != null && movieId > 0 && cinemaId != null && cinemaId > 0) {
            if (roomId != null && roomId > 0 && date != null && !date.toString().isEmpty()) {
                showTimes = showTimeRepository.findByMovieIdAndCinemaIdAndRoomIdAndShowDate(movieId, cinemaId, roomId, date, pageable);
            } else if (roomId != null && roomId > 0) {
                showTimes = showTimeRepository.findByMovieIdAndCinemaIdAndRoomId(movieId, cinemaId, roomId, pageable);
            } else if (date != null && !date.toString().isEmpty()) {
                showTimes = showTimeRepository.findByMovieIdAndShowDate(movieId, date, pageable);

            } else {
                showTimes = showTimeRepository.findByMovieIdAndCinemaId(movieId, cinemaId, pageable);
            }
        } else {
            showTimes = showTimeRepository.findAll(pageable);
        }
        // Tạo một danh sách chứa các DTO ShowTimeDto
        List<ShowTimeDto> showTimeDtos = new ArrayList<>();

        // Lặp qua các ShowTime và lấy ra thông tin movieName, cinemaName, roomName
        for (ShowTime showTime : showTimes) {
            ShowTimeDto showTimeDto = modelMapper.map(showTime, ShowTimeDto.class);

            // Lấy ra movieName từ movieId
            Movie movie = showTime.getMovie();
            if (movie != null) {
                showTimeDto.setMovieName(movie.getName());
            }
            // Lấy ra cinemaName và roomName từ room và cinemaId
            Room room = showTime.getRoom();
            if (room != null) {
                showTimeDto.setRoomName(room.getName());
                Cinema cinema = room.getCinema();
                if (cinema != null) {
                    showTimeDto.setCinemaName(cinema.getName());
                }
            }
            showTimeDtos.add(showTimeDto);
        }
        //sort by create date
        return showTimeDtos.stream().sorted(Comparator.comparing(ShowTimeDto::getCreatedDate).reversed()).toList();
    }

    @Override
    public long countAllShowTimes(String code, Long cinemaId, Long movieId, LocalDate date, Long roomId) {
        if (code != null && !code.isEmpty()) {
            return showTimeRepository.countByCode(code);
        } else if (movieId != null && movieId > 0 && cinemaId != null && cinemaId > 0) {
            if (roomId != null && roomId > 0 && date != null && !date.toString().isEmpty()) {
                return showTimeRepository.countByMovieIdAndCinemaIdAndRoomIdAndShowDate(movieId, cinemaId, roomId, date);
            } else if (roomId != null && roomId > 0) {
                return showTimeRepository.countByMovieIdAndCinemaIdAndRoomId(movieId, cinemaId, roomId);
            } else if (date != null && !date.toString().isEmpty()) {
                return showTimeRepository.countByMovieIdAndShowDate(movieId, date);
            } else {
                return showTimeRepository.countByMovieIdAndCinemaId(movieId, cinemaId);
            }
        } else {
            return showTimeRepository.count();
        }
    }


    @Override
    public void deleteShowTime(Long id) {
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Showtime not found with id: " + id, HttpStatus.NOT_FOUND));

        // Kiểm tra xem lịch chiếu đã có vé đặt chưa
        if (showTime.getSeatsBooked() > 0) {
            throw new AppException("Cannot delete showtime with booked seats.", HttpStatus.BAD_REQUEST);
        }
        if (showTime.getShowDate().isBefore(LocalDate.now()) || (showTime.getShowDate().isEqual(LocalDate.now()) && showTime.getShowTime().isBefore(LocalTime.now()))) {
            throw new AppException("Cannot delete past showtime.", HttpStatus.BAD_REQUEST);
        }
        if (showTime.isStatus()) {
            throw new AppException("Cannot delete showtime with status is active.", HttpStatus.BAD_REQUEST);
        }

        // Xóa lịch chiếu nếu chưa có vé đặt
        showTimeRepository.delete(showTime);
    }

    @Override
    public List<ShowTimeSeatDto> getShowTimeSeatById(Long id) {
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Showtime not found with id: " + id, HttpStatus.NOT_FOUND));

        Set<ShowTimeSeat> showTimeSeats = showTime.getShowTimeSeat();
        List<ShowTimeSeatDto> showTimeSeatDtos = new ArrayList<>();
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            ShowTimeSeatDto showTimeSeatDto = new ShowTimeSeatDto();
            showTimeSeatDto.setId(showTimeSeat.getId());
            showTimeSeatDto.setShowTimeId(showTimeSeat.getShowTime().getId());
            showTimeSeatDto.setStatus(showTimeSeat.isStatus());
            showTimeSeatDto.setSeat(modelMapper.map(showTimeSeat.getSeat(), SeatDto.class));
            showTimeSeat.getSeat().getSeatType().getPriceDetails().stream().findFirst().ifPresent(priceDetail -> {
                showTimeSeatDto.getSeat().setPrice(priceDetail.getPrice());
            });
            showTimeSeatDtos.add(showTimeSeatDto);
        }

        return showTimeSeatDtos;
    }

    @Override
    public void updateSeatStatus(ShowTime showTime) {

        Set<ShowTimeSeat> showTimeSeats = showTime.getShowTimeSeat();
        showTimeSeatRepository.saveAll(showTimeSeats);

    }

    @Override
    public Set<LocalDate> getShowDatesByMovieId(Long movieId) {
        List<ShowTime> showTimes = showTimeRepository.findAll();
        Set<LocalDate> showDates = new HashSet<>();
        for (ShowTime showTime : showTimes) {
            if (showTime.getMovie().getId().equals(movieId)) {
                showDates.add(showTime.getShowDate());
            }
        }
        return showDates;
    }


    public String randomCode() {
        return "LC" + LocalDateTime.now().getNano();
    }
}