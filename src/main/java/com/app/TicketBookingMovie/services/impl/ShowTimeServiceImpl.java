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
import com.app.TicketBookingMovie.services.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
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
@AllArgsConstructor
public class ShowTimeServiceImpl implements ShowTimeService {

    private final ShowTimeRepository showTimeRepository;
    private final ShowTimeSeatRepository showTimeSeatRepository;
    private final TicketService ticketService;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    public String generateKey(Long key) {
        return "ShowTime:" + key;
    }

    public void clear() {
        // xóa dữ liệu của user
        Set<Object> keyShowTime = redisTemplate.keys("ShowTime:*");
        Set<Object> keyShowTimeSeat = redisTemplate.keys("ShowTimeSeat:*");
        if (keyShowTime != null) {
            redisTemplate.delete(keyShowTime);
        }
        if (keyShowTimeSeat != null) {
            redisTemplate.delete(keyShowTimeSeat);
        }
    }

    @Transactional
    @Override
    public void createShowTime(Set<ShowTimeDto> showTimeDtos) {
        Set<ShowTimeDto> createdShowTimes = new HashSet<>();

        LocalDate currentDate = LocalDate.now().plusDays(1); // Lấy ngày hiện tại và thêm một ngày

        for (ShowTimeDto showTimeDto : showTimeDtos) {
            Room room = roomRepository.findById(showTimeDto.getRoomId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phòng chiếu với id: " + showTimeDto.getRoomId(),
                            HttpStatus.NOT_FOUND));
            Movie movie = movieRepository.findById(showTimeDto.getMovieId())
                    .orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + showTimeDto.getMovieId(),
                            HttpStatus.NOT_FOUND));
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

            // TODO: xử lý khoảng thời gian giữa các lịch chiếu trong cùng 1 ngày cùng 1
            // phòng

            List<ShowTime> existingShowTimes = showTimeRepository.findByRoomAndShowDate(room, showDate);
            existingShowTimes.sort(Comparator.comparing(ShowTime::getShowTime));
            LocalTime newShowTimeStart = showTimeDto.getShowTime();
            LocalTime newShowTimeEnd = newShowTimeStart.plusMinutes(movie.getDurationMinutes()).plusMinutes(60);
            for (ShowTime existingShowTime : existingShowTimes) {
                LocalTime existingShowTimeStart = existingShowTime.getShowTime();
                LocalTime existingShowTimeEnd = existingShowTimeStart
                        .plusMinutes(existingShowTime.getMovie().getDurationMinutes()).plusMinutes(60);
                if (newShowTimeStart.isBefore(existingShowTimeEnd) && newShowTimeEnd.isAfter(existingShowTimeStart)) {
                    throw new AppException("Không thể tạo lịch chiếu mới vì trùng với lịch chiếu đã có.",
                            HttpStatus.BAD_REQUEST);
                }
            }

            // TODO: Tạo giờ chiếu mới
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
                showTimeSeat.setHold(true);
                showTimeSeats.add(showTimeSeat);
            }
            newShowTime.setShowTimeSeat(showTimeSeats);
            showTimeRepository.save(newShowTime);
            createdShowTimes.add(modelMapper.map(newShowTime, ShowTimeDto.class));
        }
        clear();

    }

    @Override
    public ShowTimeDto getShowTimeById(Long id) {
        String key = generateKey(id);
        Object cacheData = redisTemplate.opsForValue().get(key);
        if (cacheData == null) {
            ShowTime showTime = showTimeRepository.findById(id)
                    .orElseThrow(
                            () -> new AppException("Không tìm thấy lịch chiếu với id: " + id, HttpStatus.NOT_FOUND));
            ShowTimeDto showTimeDto = modelMapper.map(showTime, ShowTimeDto.class);
            showTimeDto.setCinemaId(showTime.getRoom().getCinema().getId());
            showTimeDto.setCinemaName(showTime.getRoom().getCinema().getName());
            redisTemplate.opsForValue().set(key, showTimeDto);
            return showTimeDto;
        } else {
            // lấy name cinema
            return redisObjectMapper.convertValue(cacheData, ShowTimeDto.class);
        }
        // ShowTime showTime = showTimeRepository.findById(id)
        // .orElseThrow(() -> new AppException("Không tìm thấy lịch chiếu với id: " +
        // id, HttpStatus.NOT_FOUND));
        // ShowTimeDto showTimeDto = modelMapper.map(showTime, ShowTimeDto.class);
        // showTimeDto.setCinemaName(showTime.getRoom().getCinema().getName());
        // return showTimeDto;
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
                .orElseThrow(() -> new AppException("Không tìm thấy lịch chiếu với id: " + showTimeDto.getId(),
                        HttpStatus.NOT_FOUND));
        if (showTimeDto.getShowTime() == null) {
            showTimeDto.setShowTime(showTime.getShowTime());
        }
        if (showTimeDto.getShowDate() == null) {
            showTimeDto.setShowDate(showTime.getShowDate());
        }
        if (showTimeDto.getRoomId() == null) {
            showTimeDto.setRoomId(showTime.getRoom().getId());
        }
        if (showTimeDto.getMovieId() == null) {
            showTimeDto.setMovieId(showTime.getMovie().getId());
        }
        // Kiểm tra nếu thời gian lịch chiếu đã qua, không thể cập nhật
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        LocalDate showDate = showTime.getShowDate();
        LocalTime showTimeStart = showTime.getShowTime();
        boolean isShowTimePassed = showDate.isBefore(currentDate)
                || (showDate.isEqual(currentDate) && showTimeStart.isBefore(currentTime));
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
                        .orElseThrow(
                                () -> new AppException("Không tìm thấy phòng chiếu với id: " + showTimeDto.getRoomId(),
                                        HttpStatus.NOT_FOUND)));
                showTime.setMovie(movieRepository.findById(showTimeDto.getMovieId())
                        .orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + showTimeDto.getMovieId(),
                                HttpStatus.NOT_FOUND)));
                showTime.setStatus(showTimeDto.isStatus());
            } else {
                throw new AppException("Không thể cập nhật lịch chiếu đang hoạt động.", HttpStatus.BAD_REQUEST);
            }
        }

        showTime.setCreatedDate(LocalDateTime.now());
        // Lưu lại cập nhật vào cơ sở dữ liệu
        showTimeRepository.save(showTime);
        clear();
    }
    // kiểm tra lại danh sách ghế đã đặt

    // Xử lý nếu thời gian lịch chiếu đã qua hoặc số ghế đã đặt vượt quá số ghế của
    // phòng chiếu thì trạng thái của lịch chiếu sẽ là false
    @Async
    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    @Transactional
    public void updateShowTimeStatusAsync() {
        LocalDate currentDate = LocalDate.now(); // Ngày hiện tại
        LocalTime currentTime = LocalTime.now(); // Thời gian hiện tại
        // lấy lịch chiếu có trạng thai true
        List<ShowTime> allShowTimes = showTimeRepository.findAllByStatus(true);
        for (ShowTime showTime : allShowTimes) {
            LocalDate showDate = showTime.getShowDate();
            LocalTime showTimeStart = showTime.getShowTime();
            boolean isShowTimePassed = showDate.isBefore(currentDate)
                    || (showDate.isEqual(currentDate) && showTimeStart.isBefore(currentTime));
            // Cập nhật trạng thái của lịch chiếu
            if (isShowTimePassed) {
                showTime.setStatus(false);
                // xóa tất cả showTimeSeat
                // nếu showTimeSeat null thì bỏ qua
                if (showTime.getShowTimeSeat() != null) {
                    showTimeSeatRepository.deleteAllByShowTime(showTime);
                    showTime.setShowTimeSeat(null);
                }
                clear();
            }
            // Lưu lại trạng thái đã cập nhật vào cơ sở dữ liệu
            showTimeRepository.save(showTime);
        }
    }

    @Override
    public List<ShowTimeDto> getAllShowTimes(Integer page, Integer size, String code, Long cinemaId, Long movieId,
            LocalDate date, Long roomId) {
        String key = "ShowTime:all";
        List<ShowTime> showTimes = List.of();
        List<ShowTimeDto> showTimeDtos = new ArrayList<>();
        Object cachedData = redisTemplate.opsForValue().get(key);
        if (cachedData == null) {
            showTimes = showTimeRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
            try {
                showTimeDtos = showTimes.stream().map(showTime -> {
                    ShowTimeDto showTimeDto = new ShowTimeDto();
                    showTimeDto.setId(showTime.getId());
                    showTimeDto.setCode(showTime.getCode());
                    showTimeDto.setShowDate(showTime.getShowDate());
                    showTimeDto.setShowTime(showTime.getShowTime());
                    showTimeDto.setMovieId(showTime.getMovie().getId());
                    showTimeDto.setMovieName(showTime.getMovie().getName());
                    showTimeDto.setRoomId(showTime.getRoom().getId());
                    showTimeDto.setRoomName(showTime.getRoom().getName());
                    showTimeDto.setCinemaId(showTime.getRoom().getCinema().getId());
                    showTimeDto.setCinemaName(showTime.getRoom().getCinema().getName());
                    showTimeDto.setStatus(showTime.isStatus());
                    showTimeDto.setSeatsBooked(showTime.getSeatsBooked());
                    showTimeDto.setCreatedDate(showTime.getCreatedDate());

                    return showTimeDto;
                }).toList();
                redisTemplate.opsForValue().set(key, showTimeDtos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Chuyển đổi LinkedHashMap thành danh sách ShowTime
            List<Object> showTimeObjects = (List<Object>) cachedData;
            showTimeDtos = showTimeObjects.stream().map(obj -> redisObjectMapper.convertValue(obj, ShowTimeDto.class))
                    .toList();
        }
        // Lọc theo code, cinemaId, movieId, date, roomId nếu có
        if (code != null && !code.isEmpty()) {
            // showTimes = showTimes.stream().filter(showTime ->
            // showTime.getCode().equals(code)).toList();
            showTimeDtos = showTimeDtos.stream().filter(showTimeDto -> showTimeDto.getCode().equals(code)).toList();
        } else if (movieId != null && movieId > 0 && cinemaId != null && cinemaId > 0) {
            if (roomId != null && roomId > 0 && date != null && !date.toString().isEmpty()) {
                // showTimes = showTimes.stream().filter(showTime ->
                // showTime.getMovie().getId().equals(movieId)
                // && showTime.getRoom().getCinema().getId().equals(cinemaId) &&
                // showTime.getRoom().getId().equals(roomId)
                // && showTime.getShowDate().equals(date)).toList();
                showTimeDtos = showTimeDtos.stream().filter(showTimeDto -> showTimeDto.getMovieId().equals(movieId)
                        && showTimeDto.getCinemaId().equals(cinemaId) && showTimeDto.getRoomId().equals(roomId)
                        && showTimeDto.getShowDate().equals(date)).toList();
            } else if (roomId != null && roomId > 0) {
                // showTimes = showTimes.stream().filter(showTime ->
                // showTime.getMovie().getId().equals(movieId)
                // && showTime.getRoom().getCinema().getId().equals(cinemaId)
                // && showTime.getRoom().getId().equals(roomId)).toList();
                showTimeDtos = showTimeDtos.stream().filter(showTimeDto -> showTimeDto.getMovieId().equals(movieId)
                        && showTimeDto.getCinemaId().equals(cinemaId) && showTimeDto.getRoomId().equals(roomId))
                        .toList();

            } else if (date != null && !date.toString().isEmpty()) {
                // showTimes = showTimes.stream().filter(showTime ->
                // showTime.getMovie().getId().equals(movieId) &&
                // showTime.getRoom().getCinema().getId().equals(cinemaId) &&
                // showTime.getShowDate().equals(date)).toList();
                showTimeDtos = showTimeDtos.stream()
                        .filter(showTimeDto -> showTimeDto.getMovieId().equals(movieId)
                                && showTimeDto.getCinemaId().equals(cinemaId) && showTimeDto.getShowDate().equals(date))
                        .toList();
            } else {
                // showTimes = showTimes.stream().filter(showTime ->
                // showTime.getMovie().getId().equals(movieId) &&
                // showTime.getRoom().getCinema().getId().equals(cinemaId)).toList();
                showTimeDtos = showTimeDtos.stream().filter(showTimeDto -> showTimeDto.getMovieId().equals(movieId)
                        && showTimeDto.getCinemaId().equals(cinemaId)).toList();
            }
        }
        // // Paginate the sorted and filtered list
        int start = page * size;
        int end = Math.min(start + size, showTimeDtos.size());
        return showTimeDtos.subList(start, end);

    }

    @Override
    public long countAllShowTimes(String code, Long cinemaId, Long movieId, LocalDate date, Long roomId) {
        if (code != null && !code.isEmpty()) {
            return showTimeRepository.countByCode(code);
        } else if (movieId != null && movieId > 0 && cinemaId != null && cinemaId > 0) {
            if (roomId != null && roomId > 0 && date != null && !date.toString().isEmpty()) {
                return showTimeRepository.countByMovieIdAndCinemaIdAndRoomIdAndShowDate(movieId, cinemaId, roomId,
                        date);
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
        if (showTime.getShowDate().isBefore(LocalDate.now()) || (showTime.getShowDate().isEqual(LocalDate.now())
                && showTime.getShowTime().isBefore(LocalTime.now()))) {
            throw new AppException("Không thể xóa lịch chiếu đã qua!!!", HttpStatus.BAD_REQUEST);
        }
        if (showTime.isStatus()) {
            throw new AppException("Không thể xóa lịch chiếu đang hoạt động!!!", HttpStatus.BAD_REQUEST);
        }
        // không thể xóa lịch chiếu có Ticket
        Ticket ticket = ticketService.findByShowTime(showTime.getId());
        if (ticket != null) {
            throw new AppException("Không thể xóa lịch chiếu đã có vé đặt!!!", HttpStatus.BAD_REQUEST);
        }

        // Xóa lịch chiếu nếu chưa có vé đặt
        showTimeRepository.delete(showTime);
        clear();
    }

    @Override
    public List<ShowTimeSeatDto> getShowTimeSeatById(Long id) {

        String key = "ShowTimeSeat:" + id;
        List<ShowTimeSeatDto> showTimeSeatDtos = new ArrayList<>();
        Object cachedData = redisTemplate.opsForValue().get(key);
        if (cachedData == null) {
            ShowTime showTime = showTimeRepository.findById(id)
                    .orElseThrow(
                            () -> new AppException("Không tìm thấy lịch chiếu với id: " + id, HttpStatus.NOT_FOUND));
            Set<ShowTimeSeat> showTimeSeats = showTime.getShowTimeSeat();
            showTimeSeatDtos = showTimeSeats.stream().map(showTimeSeat -> {
                ShowTimeSeatDto showTimeSeatDto = new ShowTimeSeatDto();
                showTimeSeatDto.setId(showTimeSeat.getId());
                showTimeSeatDto.setShowTimeId(showTimeSeat.getShowTime().getId());
                showTimeSeatDto.setStatus(showTimeSeat.isStatus());
                showTimeSeatDto.setSeat(modelMapper.map(showTimeSeat.getSeat(), SeatDto.class));
                showTimeSeat.getSeat().getSeatType().getPriceDetails().stream().findFirst().ifPresent(priceDetail -> {
                    showTimeSeatDto.getSeat().setPrice(priceDetail.getPrice());
                });
                return showTimeSeatDto;
            }).toList();
            redisTemplate.opsForValue().set(key, showTimeSeatDtos);
        } else {
            // Chuyển đổi LinkedHashMap thành danh sách ShowTimeSeat
            List<Object> showTimeSeatObjects = (List<Object>) cachedData;
            showTimeSeatDtos = showTimeSeatObjects.stream()
                    .map(obj -> redisObjectMapper.convertValue(obj, ShowTimeSeatDto.class)).toList();
        }
        return showTimeSeatDtos;
    }

    @Override
    public void updateSeatStatus(ShowTime showTime) {
        Set<ShowTimeSeat> showTimeSeats = showTime.getShowTimeSeat();
        showTimeSeatRepository.saveAll(showTimeSeats);
        clear();

    }

    @Override
    public Set<LocalDate> getShowDatesByMovieId(Long movieId, Long cinemaId) {
        // lấy danh sach ngày chiếu của phim theo rạp sort theo ngày chiếu mới nhất và
        // chỉ lấy những ngày bắt đầu từ hôm nay trở đi
        // Lấy danh sách các showDate từ hôm nay trở đi bằng cách sử dụng phương thức
        // truy vấn custom
        List<LocalDate> showDates = showTimeRepository.findDistinctShowDatesByMovieIdAndCinemaIdAfterToday(movieId,
                cinemaId);
        Collections.sort(showDates);
        // Chuyển danh sách các showDate thành một tập hợp và loại bỏ trùng lặp
        return new LinkedHashSet<>(showDates);

    }

    @Override
    public void updateStatusHoldSeat(Set<Long> seatIds, Long showTimeId, boolean status) {
        // tìm ShowTimeSeat theo showTimeId và seatId
        List<ShowTimeSeat> showTimeSeats = showTimeSeatRepository.findByShowTimeIdAndSeatIdIn(showTimeId, seatIds);
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            showTimeSeat.setHold(status);
        }
        showTimeSeatRepository.saveAll(showTimeSeats);
        clear();
    }

    @Override
    public String checkSeatStatus(Set<Long> seatId, Long showTimeId) {
        // kiểm tra trạng thái của ghế
        List<ShowTimeSeat> showTimeSeats = showTimeSeatRepository.findByShowTimeIdAndSeatIdIn(showTimeId, seatId);
        // mảng lưu tên những ghế đã được giữ
        List<String> msg = new ArrayList<>();
        for (ShowTimeSeat showTimeSeat : showTimeSeats) {
            if (!showTimeSeat.isHold()) {
                msg.add(showTimeSeat.getSeat().getName());
            }

        }
        if (!msg.isEmpty()) {
            // các tên ghế cách nhau bởi dấu phẩy
            return "Ghế " + String.join(",", msg) + " đã được chọn!!!";
        } else {
            return null;
        }
    }

    public String randomCode() {
        return "LC" + LocalDateTime.now().getNano();
    }
}