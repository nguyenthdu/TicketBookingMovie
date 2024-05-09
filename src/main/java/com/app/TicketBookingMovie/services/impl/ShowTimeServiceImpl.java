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
import org.springframework.data.domain.Sort;
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
        showTime.setCreatedDate(LocalDateTime.now());
        // Lưu lại cập nhật vào cơ sở dữ liệu
        showTimeRepository.save(showTime);
    }
    //kiểm tra lại danh sách ghế đã đặt


    //Xử lý nếu thời gian lịch chiếu đã qua hoặc số ghế đã đặt vượt quá số ghế của phòng chiếu thì trạng thái của lịch chiếu sẽ là false
    @Async
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    @Transactional
    public void updateShowTimeStatusAsync() {
        LocalDate currentDate = LocalDate.now(); // Ngày hiện tại
        LocalTime currentTime = LocalTime.now(); // Thời gian hiện tại
        List<ShowTime> allShowTimes = showTimeRepository.findAll();
        for (ShowTime showTime : allShowTimes) {
            LocalDate showDate = showTime.getShowDate();
            LocalTime showTimeStart = showTime.getShowTime();
            boolean isShowTimePassed = showDate.isBefore(currentDate) || (showDate.isEqual(currentDate) && showTimeStart.isBefore(currentTime));
            // Cập nhật trạng thái của lịch chiếu
            if (isShowTimePassed) {
                showTime.setStatus(false);
                //xóa tất cả showTimeSeat
                //nếu showTimeSeat null thì bỏ qua
                if (showTime.getShowTimeSeat() != null) {
                    showTimeSeatRepository.deleteAllByShowTime(showTime);
                    showTime.setShowTimeSeat(null);
                }
            }
            // Lưu lại trạng thái đã cập nhật vào cơ sở dữ liệu
            showTimeRepository.save(showTime);
        }
    }

    @Override
    public List<ShowTimeDto> getAllShowTimes(Integer page, Integer size, String code, Long cinemaId, Long movieId, LocalDate date, Long roomId) {
        List<ShowTime> showTimes = showTimeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if (code != null && !code.isEmpty()) {
            showTimes = showTimes.stream().filter(showTime -> showTime.getCode().equals(code)).toList();
        } else if (movieId != null && movieId > 0 && cinemaId != null && cinemaId > 0) {
            if (roomId != null && roomId > 0 && date != null && !date.toString().isEmpty()) {
                showTimes = showTimes.stream().filter(showTime -> showTime.getMovie().getId().equals(movieId)
                        && showTime.getRoom().getCinema().getId().equals(cinemaId) && showTime.getRoom().getId().equals(roomId)
                        && showTime.getShowDate().equals(date)).toList();
            } else if (roomId != null && roomId > 0) {
                showTimes = showTimes.stream().filter(showTime -> showTime.getMovie().getId().equals(movieId)
                        && showTime.getRoom().getCinema().getId().equals(cinemaId)
                        && showTime.getRoom().getId().equals(roomId)).toList();
            } else if (date != null && !date.toString().isEmpty()) {
                showTimes = showTimes.stream().filter(showTime -> showTime.getMovie().getId().equals(movieId) && showTime.getRoom().getCinema().getId().equals(cinemaId) && showTime.getShowDate().equals(date)).toList();
            } else {
                showTimes = showTimes.stream().filter(showTime -> showTime.getMovie().getId().equals(movieId) && showTime.getRoom().getCinema().getId().equals(cinemaId)).toList();
            }
        }
        // // Paginate the sorted and filtered list
        int start = page * size;
        int end = Math.min(start + size, showTimes.size());
        List<ShowTime> pagedShowTimes = showTimes.subList(start, end);
        //map dto
        return pagedShowTimes.stream().map(showTime -> {
            ShowTimeDto showTimeDto = modelMapper.map(showTime, ShowTimeDto.class);
            showTimeDto.setCinemaName(showTime.getRoom().getCinema().getName());
            return showTimeDto;
        }).toList();
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
                .orElseThrow(() -> new AppException("Không tìm thấy lịch chiếu với id: " + id, HttpStatus.NOT_FOUND));

        // Kiểm tra xem lịch chiếu đã có vé đặt chưa
        if (showTime.getSeatsBooked() > 0) {
            throw new AppException("Không thể xóa lịch chiếu đã có người đặt vé!!!", HttpStatus.BAD_REQUEST);
        }
        if (showTime.getShowDate().isBefore(LocalDate.now()) || (showTime.getShowDate().isEqual(LocalDate.now()) && showTime.getShowTime().isBefore(LocalTime.now()))) {
            throw new AppException("Không thể xóa lịch chiếu đã qua!!!", HttpStatus.BAD_REQUEST);
        }
        if (showTime.isStatus()) {
            throw new AppException("Không thể xóa lịch chiếu đang hoạt động!!!", HttpStatus.BAD_REQUEST);
        }
        // Xóa lịch chiếu nếu chưa có vé đặt
        showTimeRepository.delete(showTime);
    }

    @Override
    public List<ShowTimeSeatDto> getShowTimeSeatById(Long id) {
        ShowTime showTime = showTimeRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy: " + id, HttpStatus.NOT_FOUND));

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
    public Set<LocalDate> getShowDatesByMovieId(Long movieId, Long cinemaId) {
        //lấy danh sach ngày chiếu của phim theo rạp sort theo ngày chiếu mới nhất và chỉ lấy những ngày bắt đầu từ hôm nay trở đi
        // Lấy danh sách các showDate từ hôm nay trở đi bằng cách sử dụng phương thức truy vấn custom
        List<LocalDate> showDates = showTimeRepository.findDistinctShowDatesByMovieIdAndCinemaIdAfterToday(movieId, cinemaId);
        Collections.sort(showDates);
        // Chuyển danh sách các showDate thành một tập hợp và loại bỏ trùng lặp
        return new LinkedHashSet<>(showDates);


    }

    @Override
    public void updateStatusHoldSeat(Set<Long> seatIds,Long showTimeId, boolean status) {
        //tìm ShowTimeSeat theo showTimeId và seatId
        List<ShowTimeSeat> showTimeSeats = showTimeSeatRepository.findByShowTimeIdAndSeatIdIn(showTimeId, seatIds);
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            showTimeSeat.setStatus(status);
        }
        showTimeSeatRepository.saveAll(showTimeSeats);
    }

    @Override
    public String checkSeatStatus(Set<Long> seatId, Long showTimeId) {
        //kiểm tra trạng thái của ghế
        List<ShowTimeSeat> showTimeSeats = showTimeSeatRepository.findByShowTimeIdAndSeatIdIn(showTimeId, seatId);
        //mảng lưu tên những ghế đã được giữ
        List<String> msg = new ArrayList<>();
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            if(!showTimeSeat.isStatus()){
                msg.add(showTimeSeat.getSeat().getName());
            }

        }
      if(!msg.isEmpty()){
//          các tên ghế cách nhau bởi dấu phẩy
            return "Ghế "+ String.join(",", msg) + " đã được chọn!!!";
      }
      else{
          return null;
      }
    }

    public String randomCode() {
        return "LC" + LocalDateTime.now().getNano();
    }
}