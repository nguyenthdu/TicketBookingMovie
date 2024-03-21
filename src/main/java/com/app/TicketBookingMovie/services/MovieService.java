package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.MovieDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MovieService {
    void createMovie(MovieDto movieDTO, MultipartFile file) throws IOException;
    MovieDto getMovieById(Long id);
    void updateMovieById(MovieDto movieDTO, MultipartFile multipartFile) throws IOException;
    void deleteMovieById(Long id);
    List<MovieDto> getAllMovies(Integer page, Integer size, String code, String name, Long genreId, Long cinemaId);

    long countAllMovies(String code, String name, Long genreId, Long cinemaId);
}
