package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.MovieDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Cinema;
import com.app.TicketBookingMovie.models.Genre;
import com.app.TicketBookingMovie.models.Movie;
import com.app.TicketBookingMovie.repository.CinemaRepository;
import com.app.TicketBookingMovie.repository.GenreRepository;
import com.app.TicketBookingMovie.repository.MovieRepository;
import com.app.TicketBookingMovie.services.AwsService;
import com.app.TicketBookingMovie.services.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieServiceImpl implements MovieService {
    private final ModelMapper modelMapper;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CinemaRepository cinemaRepository;
    private final AwsService awsService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;

    // random code
    public String randomCode() {
        return "PI" + LocalDateTime.now().getNano();
    }

    public String generateKey(Long key) {
        return "Movie:" + key;
    }

    public void clear() {
        //xóa dữ liệu của user
        Set<Object> keys = redisTemplate.keys("Movie:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void createMovie(MovieDto movieDTO) {
        Movie movie = modelMapper.map(movieDTO, Movie.class);
        movie.setCode(randomCode());
        movie.setImageLink(movieDTO.getImageLink());
        // Chuyển đổi id của thể loại phim sang các đối tượng Genre
        Set<Genre> genres = new HashSet<>();
        for (Long genreId : movieDTO.getGenreIds()) {
            Optional<Genre> genreOptional = Optional.ofNullable(genreRepository.findById(genreId).orElseThrow(() -> new AppException("Không tìm thấy thể loại phim với id:  " + genreId, HttpStatus.NOT_FOUND)));
            genreOptional.ifPresent(genres::add);
        }
        movie.setGenres(genres);
        //Chuyển đổi id cinema sang các đối tượng Cinema
        Set<Cinema> cinemas = new HashSet<>();
        for (Long cinemeId : movieDTO.getCinemaIds()) {
            Optional<Cinema> cinemaOptional = Optional.ofNullable(cinemaRepository.findById(cinemeId).orElseThrow(() -> new AppException("Không tìm thấy rạp với id: " + cinemeId, HttpStatus.NOT_FOUND)));
            cinemaOptional.ifPresent(cinemas::add);
        }
        movie.setCinemas(cinemas);
        //nếu trạng thái của rạp là false thì trạng thái phim không thể là true
        if (!movie.isStatus() && movieDTO.isStatus()) {
            throw new AppException("Không thể đặt trạng thái phim hoạt động khi trạng thái rạp không hoạt động ", HttpStatus.BAD_REQUEST);
        }
        movie.setCreatedDate(LocalDateTime.now());
        movieRepository.save(movie);
        clear();
    }

    @Override
    public MovieDto getMovieById(Long id) {
        String key = generateKey(id);
        Object cachedDate = redisTemplate.opsForValue().get(key);
        if (cachedDate == null) {
            Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + id, HttpStatus.NOT_FOUND));
            MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
            Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
            Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
            movieDTO.setCinemaIds(cinemaIds);
            movieDTO.setGenreIds(genreIds);

            redisTemplate.opsForValue().set(key, movieDTO);
            return movieDTO;
        } else {
            return redisObjectMapper.convertValue(cachedDate, MovieDto.class);
//        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + id, HttpStatus.NOT_FOUND))
        }
//        Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
//        Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
//        MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
//        movieDTO.setCinemaIds(cinemaIds);
//        movieDTO.setGenreIds(genreIds);
//        return movieDTO;

    }

    @Override
    public Movie findById(Long id) {
        return movieRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void updateMovieById(MovieDto movieDTO) {
        Movie movie = movieRepository.findById(movieDTO.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + movieDTO.getId(), HttpStatus.NOT_FOUND));
        // Xử lý ảnh
        if (!movieDTO.getImageLink().isEmpty() && !movieDTO.getImageLink().isBlank() && !movieDTO.getImageLink().equals(movie.getImageLink())) {
            awsService.deleteImage(movie.getImageLink());
            movie.setImageLink(movieDTO.getImageLink());
        } else {
            movie.setImageLink(movie.getImageLink());
        }
        // Chuyển đổi id của thể loại phim sang các đối tượng Genre
        if (!movieDTO.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>();
            for (Long genreId : movieDTO.getGenreIds()) {
                Optional<Genre> genreOptional = Optional.ofNullable(genreRepository.findById(genreId).orElseThrow(() -> new AppException("Genre not found with id: " + genreId, HttpStatus.NOT_FOUND)));
                genreOptional.ifPresent(genres::add);
            }
            movie.setGenres(genres);
        } else {
            movie.setGenres(movie.getGenres());
        }
        //Chuyển đổi id cinema sang các đối tượng Cinema
        if (!movieDTO.getCinemaIds().isEmpty()) {
            Set<Cinema> cinemas = new HashSet<>();
            for (Long cinemeId : movieDTO.getCinemaIds()) {
                Optional<Cinema> cinemaOptional = Optional.ofNullable(cinemaRepository.findById(cinemeId).orElseThrow(() -> new AppException("Cinema not found with id: " + cinemeId, HttpStatus.NOT_FOUND)));
                cinemaOptional.ifPresent(cinemas::add);
            }
            movie.setCinemas(cinemas);
        } else {
            movie.setCinemas(movie.getCinemas());
        }
        if (!movieDTO.getName().isEmpty() && !movieDTO.getName().isBlank()) {
            movie.setName(movieDTO.getName());
        } else {
            movie.setName(movie.getName());
        }
        if (!movieDTO.getTrailerLink().isEmpty() && !movieDTO.getTrailerLink().isBlank() && !movieDTO.getTrailerLink().equals(movie.getTrailerLink())) {
            awsService.deleteImage(movie.getTrailerLink());
            movie.setTrailerLink(movieDTO.getTrailerLink());
        } else {
            movie.setTrailerLink(movie.getTrailerLink());
        }
        if (!movieDTO.getDescription().isEmpty() && !movieDTO.getDescription().isBlank()) {
            movie.setDescription(movieDTO.getDescription());
        } else {
            movie.setDescription(movie.getDescription());
        }
        if (movieDTO.getDurationMinutes() > 0) {
            movie.setDurationMinutes(movieDTO.getDurationMinutes());
        } else {
            movie.setDurationMinutes(movie.getDurationMinutes());
        }
        if (movieDTO.getReleaseDate() != null) {
            movie.setReleaseDate(movieDTO.getReleaseDate());
        } else {
            movie.setReleaseDate(movie.getReleaseDate());
        }
        if (movieDTO.isStatus() != movie.isStatus()) {
            movie.setStatus(movieDTO.isStatus());
        } else {
            movie.setStatus(movie.isStatus());
        }
        if (!movieDTO.getCountry().isEmpty() && !movieDTO.getCountry().isBlank()) {
            movie.setCountry(movieDTO.getCountry());
        } else {
            movie.setCountry(movie.getCountry());
        }
        if (!movieDTO.getDirector().isEmpty() && !movieDTO.getDirector().isBlank()) {
            movie.setDirector(movieDTO.getDirector());
        } else {
            movie.setDirector(movie.getDirector());
        }
        if (!movieDTO.getCast().isEmpty() && !movieDTO.getCast().isBlank()) {
            movie.setCast(movieDTO.getCast());
        } else {
            movie.setCast(movie.getCast());
        }
        if (!movieDTO.getProducer().isEmpty() && !movieDTO.getProducer().isBlank()) {
            movie.setProducer(movieDTO.getProducer());
        } else {
            movie.setProducer(movie.getProducer());
        }
        movie.setCreatedDate(LocalDateTime.now());
        movieRepository.save(movie);
        clear();
    }

    @Override
    @Transactional
    public void deleteMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phim với id: " + id, HttpStatus.NOT_FOUND));

        // Kiểm tra nếu có lịch chiếu
        if (!movie.getShowTimes().isEmpty()) {
            throw new AppException("Không thể xóa phim có lịch chiếu", HttpStatus.BAD_REQUEST);
        }

        // Nếu không có lịch chiếu, thì tiến hành xóa
        awsService.deleteImage(movie.getImageLink());

        // Xóa phim từ cơ sở dữ liệu
        movieRepository.delete(movie);

        // Sau khi xóa thành công khỏi cơ sở dữ liệu, tiến hành xóa dữ liệu trên Redis
        clear();
    }


    @Override
    public List<MovieDto> getAllMovies(Integer page, Integer size, String code, String name, Long genreId, Long cinemaId, String typeShow) {
        String key  = "Movie:all";
        List<Movie> allMovies = new ArrayList<>();
        List<MovieDto> movieDtos;
        Object cachedData = redisTemplate.opsForValue().get(key);
        if(cachedData==null){
            allMovies = movieRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
            movieDtos = allMovies.stream()
                    .map(movie -> {
                        MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
                        Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
                        Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
                        movieDTO.setGenreIds(genreIds);
                        movieDTO.setCinemaIds(cinemaIds);
                        return movieDTO;
                    }).collect(Collectors.toList());
            redisTemplate.opsForValue().set(key, movieDtos);
        }else{
            List<Object> list = (List<Object>) cachedData;
            movieDtos = list.stream().map(o -> redisObjectMapper.convertValue(o, MovieDto.class)).toList();
        }


        // Lấy danh sách tất cả các phim
        if ("Upcoming".equalsIgnoreCase(typeShow)) {
            allMovies = movieRepository.findAll(Sort.by(Sort.Direction.DESC, "releaseDate"))
                    .stream()
                    .filter(movie -> movie.getShowTimes().stream().allMatch(showTime -> showTime.getShowDate().isAfter(LocalDate.now())))
                    .toList();
            movieDtos = allMovies.stream()
                    .map(movie -> {
                        MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
                        Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
                        Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
                        movieDTO.setGenreIds(genreIds);
                        movieDTO.setCinemaIds(cinemaIds);
                        return movieDTO;
                    }).collect(Collectors.toList());


        } else if ("Showing".equalsIgnoreCase(typeShow)) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Movie> allMoviesPage = movieRepository.findAll(pageable);
            allMovies = allMoviesPage.getContent()
                    .stream()
                    .filter(movie -> movie.getShowTimes().stream().anyMatch(showTime -> showTime.getShowDate().isEqual(LocalDate.now()) || showTime.getShowDate().isBefore(LocalDate.now())))
                    .toList();
            movieDtos = allMovies.stream().map(movie -> {
                MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
                Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
                Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
                movieDTO.setGenreIds(genreIds);
                movieDTO.setCinemaIds(cinemaIds);
                return movieDTO;
            }).collect(Collectors.toList());

        }

        // Lọc theo các tiêu chí khác nếu có
        if (code != null && !code.isEmpty()) {
//            allMovies = allMovies.stream().filter(movie -> movie.getCode().equals(code)).collect(Collectors.toList());
            movieDtos = movieDtos.stream().filter(movie -> movie.getCode().equals(code)).collect(Collectors.toList());
        } else if (cinemaId != null && cinemaId != 0) {
            if (name != null && !name.isEmpty()) {
//                allMovies = allMovies.stream().filter(movie -> movie.getCinemas().stream().anyMatch(cinema -> cinema.getId().equals(cinemaId)) && movie.getName().toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
                movieDtos = movieDtos.stream().filter(movie -> movie.getCinemaIds().contains(cinemaId) && movie.getName().toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
            } else if (genreId != null && genreId != 0) {
//                allMovies = allMovies.stream().filter(movie -> movie.getCinemas().stream().anyMatch(cinema -> cinema.getId().equals(cinemaId)) && movie.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId))).collect(Collectors.toList());
                movieDtos = movieDtos.stream().filter(movie -> movie.getCinemaIds().contains(cinemaId) && movie.getGenreIds().contains(genreId)).collect(Collectors.toList());
            } else {
//                allMovies = allMovies.stream().filter(movie -> movie.getCinemas().stream().anyMatch(cinema -> cinema.getId().equals(cinemaId))).collect(Collectors.toList());
                movieDtos = movieDtos.stream().filter(movie -> movie.getCinemaIds().contains(cinemaId)).collect(Collectors.toList());
            }
        } else if (genreId != null && genreId != 0) {
//            allMovies = allMovies.stream().filter(movie -> movie.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId))).collect(Collectors.toList());
            movieDtos = movieDtos.stream().filter(movie -> movie.getGenreIds().contains(genreId)).collect(Collectors.toList());
        } else if (name != null && !name.isEmpty()) {
//            allMovies = allMovies.stream().filter(movie -> movie.getName().contains(name)).collect(Collectors.toList());
            movieDtos = movieDtos.stream().filter(movie -> movie.getName().contains(name)).collect(Collectors.toList());
        }
    int fromIndex = page * size;
    int toIndex = Math.min(fromIndex + size, movieDtos.size());
    return movieDtos.subList(fromIndex, toIndex);
//        // Đếm số lượng phim
//        long countAll = allMovies.size();
//
//        // Phân trang
//        int fromIndex = page * size;
//        int toIndex = Math.min(fromIndex + size, allMovies.size());
//        List<MovieDto> movieDtos = allMovies.subList(fromIndex, toIndex).stream()
//                .map(movie -> {
//                    MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
//                    Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
//                    Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
//                    movieDTO.setGenreIds(genreIds);
//                    movieDTO.setCinemaIds(cinemaIds);
//                    return movieDTO;
//                }).collect(Collectors.toList());
//
//
//        // Trả về danh sách phim và số lượng phim
//        return movieDtos;
    }

    @Override
    public long countAllMovies(String code, String name, Long genreId, Long cinemaId, String typeShow) {
        List<Movie> allMovies = new ArrayList<>();

        // Lấy danh sách tất cả các phim
        if ("Upcoming".equalsIgnoreCase(typeShow)) {
            allMovies = movieRepository.findAll(Sort.by(Sort.Direction.DESC, "releaseDate"))
                    .stream()
                    .filter(movie -> movie.getShowTimes().stream().allMatch(showTime -> showTime.getShowDate().isAfter(LocalDate.now())))
                    .toList();
        } else if ("Showing".equalsIgnoreCase(typeShow)) {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE); // Lấy tất cả phim đang chiếu
            Page<Movie> allMoviesPage = movieRepository.findAll(pageable);
            allMovies = allMoviesPage.getContent()
                    .stream()
                    .filter(movie -> movie.getShowTimes().stream().anyMatch(showTime -> showTime.getShowDate().isEqual(LocalDate.now()) || showTime.getShowDate().isBefore(LocalDate.now())))
                    .toList();
        } else {
            allMovies = movieRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        }

        // Lọc theo các tiêu chí khác nếu có
        if (code != null && !code.isEmpty()) {
            allMovies = allMovies.stream().filter(movie -> movie.getCode().equals(code)).collect(Collectors.toList());
        } else if (cinemaId != null && cinemaId != 0) {
            if (name != null && !name.isEmpty()) {
                allMovies = allMovies.stream().filter(movie -> movie.getCinemas().stream().anyMatch(cinema -> cinema.getId().equals(cinemaId)) && movie.getName().toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
            } else if (genreId != null && genreId != 0) {
                allMovies = allMovies.stream().filter(movie -> movie.getCinemas().stream().anyMatch(cinema -> cinema.getId().equals(cinemaId)) && movie.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId))).collect(Collectors.toList());
            } else {
                allMovies = allMovies.stream().filter(movie -> movie.getCinemas().stream().anyMatch(cinema -> cinema.getId().equals(cinemaId))).collect(Collectors.toList());
            }
        } else if (genreId != null && genreId != 0) {
            allMovies = allMovies.stream().filter(movie -> movie.getGenres().stream().anyMatch(genre -> genre.getId().equals(genreId))).collect(Collectors.toList());
        } else if (name != null && !name.isEmpty()) {
            allMovies = allMovies.stream().filter(movie -> movie.getName().contains(name)).collect(Collectors.toList());
        }

        // Đếm số lượng phim
        return allMovies.size();
    }

    //TODO: Lấy danh sách phim sắp chiếu
    @Override
    public List<MovieDto> getMoviesUpcoming(Integer page, Integer size) {
        // Lấy danh sách tất cả các phim
        List<Movie> allMovies = movieRepository.findAll(Sort.by(Sort.Direction.DESC, "releaseDate"));
        // Lọc ra các phim sắp chiếu
        List<Movie> moviesNotShowed = allMovies.stream()
                .filter(movie -> movie.getShowTimes().stream()
                        .allMatch(showTime -> showTime.getShowDate().isAfter(LocalDate.now())))
                .toList();

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, moviesNotShowed.size());
        return moviesNotShowed.subList(fromIndex, toIndex).stream()
                .map(movie -> modelMapper.map(movie, MovieDto.class))
                .collect(Collectors.toList());
    }

    //TODO: Lấy danh sách phim đang chiếu
    @Override
    public List<MovieDto> getMoviesShowing(Integer page, Integer size) {
        // Lấy danh sách tất cả các phim
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> allMovies = movieRepository.findAll(pageable);
        // Lọc ra các phim đang chiếu
        List<Movie> moviesShowing = allMovies.stream()
                .filter(movie -> movie.getShowTimes().stream()
                        .anyMatch(showTime -> showTime.getShowDate().isEqual(LocalDate.now()) || showTime.getShowDate().isBefore(LocalDate.now())))
                .toList();
        // Chuyển đổi sang MovieDto và trả về
        return moviesShowing.stream()
                .map(movie -> modelMapper.map(movie, MovieDto.class))
                .collect(Collectors.toList());
    }


}
