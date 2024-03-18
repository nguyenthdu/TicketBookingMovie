package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.app.TicketBookingMovie.dtos.MovieDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Genre;
import com.app.TicketBookingMovie.models.Movie;
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
    private final AmazonS3 amazonS3;

    public MovieServiceImpl(ModelMapper modelMapper, MovieRepository movieRepository, GenreRepository genreRepository, AmazonS3 amazonS3) {
        this.modelMapper = modelMapper;
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.amazonS3 = amazonS3;
    }

    private static final long MAX_SIZE = 10 * 1024 * 1024;

    // random code
    public String randomCode() {
        Random random = new Random();
        String code;
        int number = random.nextInt(1000);
        code = "PHIM" + System.currentTimeMillis() + number;
        return code;
    }

    //methods check type file
    public void checkFileType(MultipartFile multipartFile) {
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!fileType.equals(".jpg") && !fileType.equals(".png")) {
            throw new IllegalArgumentException("Only .jpg and .png files are allowed");
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
    public MovieDTO createMovie(MovieDTO movieDTO, MultipartFile multipartFile) throws IOException {
        String code = randomCode();
        checkFileType(multipartFile);
        if (multipartFile.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size is too large");
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
        movieRepository.save(movie);
        return modelMapper.map(movie, MovieDTO.class);
    }

    @Override
    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException("Movie not found with id: " + id, HttpStatus.NOT_FOUND));
        Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        MovieDTO movieDTO = modelMapper.map(movie, MovieDTO.class);
        movieDTO.setGenreIds(genreIds);
        return movieDTO;

    }

    @Override
    public MovieDTO updateMovieById(MovieDTO movieDTO, MultipartFile multipartFile) throws IOException {
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
            String newImageName = randomCode() + "_" + LocalDateTime.now() + getFileExtension(multipartFile.getOriginalFilename());
            File newImageFile = convertMultiPartFileToFile(multipartFile);
            amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_MOVIE, newImageName, newImageFile));
            newImageFile.delete();
            String newImageLink = amazonS3.getUrl(BUCKET_NAME_MOVIE, newImageName).toString();
            // Cập nhật link hình ảnh mới trong movieDTO
            movie.setImageLink(newImageLink);
        }
        // Chuyển đổi id của thể loại phim sang các đối tượng Genre
        Set<Genre> genres = new HashSet<>();
        for (Long genreId : movieDTO.getGenreIds()) {
            Optional<Genre> genreOptional = Optional.ofNullable(genreRepository.findById(genreId).orElseThrow(() -> new AppException("Genre not found with id: " + genreId, HttpStatus.NOT_FOUND)));
            genreOptional.ifPresent(genres::add);
        }
        movie.setGenres(genres);
        movie.setName(movieDTO.getName());
        movie.setTrailerLink(movieDTO.getTrailerLink());
        movie.setDescription(movieDTO.getDescription());
        movie.setDurationMinutes(movieDTO.getDurationMinutes());
        movie.setReleaseDate(movieDTO.getReleaseDate());
        movie.setCountry(movieDTO.getCountry());
        movie.setDirector(movieDTO.getDirector());
        movie.setCast(movieDTO.getCast());
        movie.setProducer(movieDTO.getProducer());
        movieRepository.save(movie);
        return modelMapper.map(movie, MovieDTO.class);
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
    public List<MovieDTO> getAllMovies(Integer page, Integer size, String code, String name, Long genreId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> movies;
        if (code != null && !code.isEmpty()) {
            movies = movieRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            movies = movieRepository.findByNameContaining(name, pageable);
        } else if (genreId != null && genreId != 0) {
            movies = movieRepository.findByGenreId(genreId, pageable);
        } else {
            movies = movieRepository.findAll(pageable);
        }
        return movies.stream().map(movie -> {
            MovieDTO movieDTO = modelMapper.map(movie, MovieDTO.class);
            Set<Long> genreIds = movie.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
            movieDTO.setGenreIds(genreIds);
            return movieDTO;
        }).collect(Collectors.toList());
    }
}
