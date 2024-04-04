package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.app.TicketBookingMovie.dtos.MovieDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Cinema;
import com.app.TicketBookingMovie.models.Genre;
import com.app.TicketBookingMovie.models.Movie;
import com.app.TicketBookingMovie.repository.CinemaRepository;
import com.app.TicketBookingMovie.repository.GenreRepository;
import com.app.TicketBookingMovie.repository.MovieRepository;
import com.app.TicketBookingMovie.services.MovieService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
    @Value("${S3_BUCKET_NAME_MOVIE}")
    private String BUCKET_NAME_MOVIE;
    private final ModelMapper modelMapper;
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CinemaRepository cinemaRepository;
    private final AmazonS3 amazonS3;
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    public MovieServiceImpl(ModelMapper modelMapper, MovieRepository movieRepository, GenreRepository genreRepository, AmazonS3 amazonS3, CinemaRepository cinemaRepository) {
        this.modelMapper = modelMapper;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.amazonS3 = amazonS3;
        this.cinemaRepository = cinemaRepository;
    }


    // random code
    public String randomCode() {
        return "PI" + LocalDateTime.now().getNano();
    }

    //methods check type file
    public void checkFileType(MultipartFile multipartFile) {
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!fileType.equals(".jpg") && !fileType.equals(".png")) {
            throw new AppException("Only .jpg and .png files are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }

    @Override
    public void createMovie(MovieDto movieDTO, MultipartFile multipartFile) throws IOException {
        String code = randomCode();
        checkFileType(multipartFile);
        if (multipartFile.getSize() > MAX_SIZE) {
            throw new AppException("File size is too large. must < 10mb", HttpStatus.BAD_REQUEST);
        }
        String image = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = image.substring(image.lastIndexOf("."));
        String fileName = code + "_" + LocalDateTime.now() + fileType;
        File file = convertMultiPartFileToFile(multipartFile);
        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_MOVIE, fileName, file));
        file.delete();
        String uploadLink = amazonS3.getUrl(BUCKET_NAME_MOVIE, fileName).toString();
        Movie movie = modelMapper.map(movieDTO, Movie.class);
        movie.setCode(code);
        movie.setImageLink(uploadLink);
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
        MovieDto movieDTO = modelMapper.map(movie, MovieDto.class);
        movieDTO.setGenreIds(genreIds);
        return movieDTO;

    }

    @Override
    public void updateMovieById(MovieDto movieDTO, MultipartFile multipartFile) throws IOException {
        Movie movie = movieRepository.findById(movieDTO.getId())
                .orElseThrow(() -> new AppException("Movie not found with id: " + movieDTO.getId(), HttpStatus.NOT_FOUND));

        // Kiểm tra xem có hình ảnh mới được cung cấp không
        if (multipartFile != null && !multipartFile.isEmpty()) {
            // Xóa hình ảnh cũ trên AWS
            String imageUrl = movie.getImageLink();
            String imageKey = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            imageKey = imageKey.replace("%3A", ":");
            amazonS3.deleteObject(BUCKET_NAME_MOVIE, imageKey);
            // Lưu hình ảnh mới lên AWS
            String newImageName = movie.getCode() + "_" + LocalDateTime.now() + getFileExtension(multipartFile.getOriginalFilename());
            File newImageFile = convertMultiPartFileToFile(multipartFile);
            amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_MOVIE, newImageName, newImageFile));
            newImageFile.delete();
            String newImageLink = amazonS3.getUrl(BUCKET_NAME_MOVIE, newImageName).toString();
            // Cập nhật link hình ảnh mới trong movieDTO
            movie.setImageLink(newImageLink);
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
        if (!movieDTO.getTrailerLink().isEmpty() && !movieDTO.getTrailerLink().isBlank()) {
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
        modelMapper.map(movie, MovieDto.class);
    }

    // Hàm tiện ích để lấy phần mở rộng của file
    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public void deleteMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException("Movie not found with id: " + id, HttpStatus.NOT_FOUND));
        // Xóa hình ảnh của phim trên AWS
        String imageUrl = movie.getImageLink();
        String imageKey = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        imageKey = imageKey.replace("%3A", ":");
        amazonS3.deleteObject(BUCKET_NAME_MOVIE, imageKey);
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
                .map(movie -> modelMapper.map(movie, MovieDto.class))
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
}
