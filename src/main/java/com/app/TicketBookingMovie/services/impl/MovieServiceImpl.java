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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private final ModelMapper modelMapper;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CinemaRepository cinemaRepository;
    private final AwsService awsService;

    public MovieServiceImpl(ModelMapper modelMapper, MovieRepository movieRepository, GenreRepository genreRepository, CinemaRepository cinemaRepository, AwsService awsService) {
        this.modelMapper = modelMapper;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.awsService = awsService;
        this.cinemaRepository = cinemaRepository;
    }


    // random code
    public String randomCode() {
        return "PI" + LocalDateTime.now().getNano();
    }


    @Override
    public void createMovie(MovieDto movieDTO) {


        Movie movie = modelMapper.map(movieDTO, Movie.class);
        movie.setCode(randomCode());
        movie.setImageLink(movieDTO.getImageLink());
        // Chuyển đổi id của thể loại phim sang các đối tượng Genre
        Set<Genre> genres = new HashSet<>();
        for (Long genreId : movieDTO.getGenreIds()) {
            Optional<Genre> genreOptional = Optional.ofNullable(genreRepository.findById(genreId).orElseThrow(() -> new AppException("Genre not found with id: " + genreId, HttpStatus.NOT_FOUND)));
            genreOptional.ifPresent(genres::add);
        }
        movie.setGenres(genres);
        //Chuyển đổi id cinema sang các đối tượng Cinema
        Set<Cinema> cinemas = new HashSet<>();
        for (Long cinemeId : movieDTO.getCinemaIds()) {
            Optional<Cinema> cinemaOptional = Optional.ofNullable(cinemaRepository.findById(cinemeId).orElseThrow(() -> new AppException("Cinema not found with id: " + cinemeId, HttpStatus.NOT_FOUND)));
            cinemaOptional.ifPresent(cinemas::add);
        }
        movie.setCinemas(cinemas);
        movie.setStatus(movieDTO.isStatus());
        movie.setCreatedDate(LocalDateTime.now());
        movieRepository.save(movie);
        modelMapper.map(movie, MovieDto.class);
    }

    @Override
    public MovieDto getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException("Movie not found with id: " + id, HttpStatus.NOT_FOUND));
        Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Long> cinemaIds = movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet());
        MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
        movieDTO.setCinemaIds(cinemaIds);
        movieDTO.setGenreIds(genreIds);
        return movieDTO;

    }

    @Override
    public void updateMovieById(MovieDto movieDTO) {
        Movie movie = movieRepository.findById(movieDTO.getId())
                .orElseThrow(() -> new AppException("Movie not found with id: " + movieDTO.getId(), HttpStatus.NOT_FOUND));

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
        movieRepository.save(movie);
    }

    @Override
    public void deleteMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Movie not found with id: " + id, HttpStatus.NOT_FOUND));
        awsService.deleteImage(movie.getImageLink());
        // Xóa phim từ cơ sở dữ liệu
        movieRepository.delete(movie);
    }

    @Override
    public List<MovieDto> getAllMovies(Integer page, Integer size, String code, String name, Long genreId, Long cinemaId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies;
        if (code != null && !code.isEmpty()) {
            movies = movieRepository.findByCodeContaining(code, pageable);
        } else if (cinemaId != null && cinemaId != 0) {
            if (name != null && !name.isEmpty()) {
                movies = movieRepository.findByCinemasIdAndNameContaining(cinemaId, name, pageable);
            } else if (genreId != null && genreId != 0) {
                movies = movieRepository.findByCinemasIdAndGenreId(cinemaId, genreId, pageable);
            } else {
                movies = movieRepository.findByCinemaId(cinemaId, pageable);
            }
        } else if (genreId != null && genreId != 0) {
            movies = movieRepository.findByGenreId(genreId, pageable);
        } else if (name != null && !name.isEmpty()) {
            movies = movieRepository.findByNameContaining(name, pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }


        //sort by created date
        return movies.stream().sorted(Comparator.comparing(Movie::getCreatedDate).reversed())
                .map(movie ->{

                    MovieDto movieDto = modelMapper.map(movie, MovieDto.class);
                    movieDto.setCinemaIds(movie.getCinemas().stream().map(Cinema::getId).collect(Collectors.toSet()));
                    movieDto.setGenreIds(movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()));
                    return movieDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public long countAllMovies(String code, String name, Long genreId, Long cinemaId) {
        if (code != null && !code.isEmpty()) {
            return movieRepository.countByCodeContaining(code);
        } else if (cinemaId != null && cinemaId != 0) {
            if (name != null && !name.isEmpty()) {
                return movieRepository.countByCinemasIdAndNameContaining(cinemaId, name);
            } else if (genreId != null && genreId != 0) {
                return movieRepository.countByCinemasIdAndGenreId(cinemaId, genreId);
            } else {
                return movieRepository.countByCinemaId(cinemaId);
            }
        } else if (genreId != null && genreId != 0) {
            return movieRepository.countByGenreId(genreId);
        } else if (name != null && !name.isEmpty()) {
            return movieRepository.countByNameContaining(name);
        } else {
            return movieRepository.count();
        }
    }

    @Override
    public List<MovieDto> getMoviesUpcoming(Integer page, Integer size) {
        // Lấy danh sách tất cả các phim
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> allMovies = movieRepository.findAll(pageable);

        // Lọc ra các phim sắp chiếu
        List<Movie> moviesNotShowed = allMovies.stream()
                .filter(movie -> movie.getShowTimes().stream()
                        .allMatch(showTime -> showTime.getShowDate().isAfter(LocalDate.now())))
                .toList();

        // Chuyển đổi sang MovieDto và trả về
        return moviesNotShowed.stream()
                .map(movie -> modelMapper.map(movie, MovieDto.class))
                .collect(Collectors.toList());
    }

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
