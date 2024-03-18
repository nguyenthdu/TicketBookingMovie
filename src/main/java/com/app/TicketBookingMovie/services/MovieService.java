package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.MovieDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MovieService {
    MovieDTO createMovie(MovieDTO movieDTO, MultipartFile file) throws IOException;
    MovieDTO getMovieById(Long id);
    MovieDTO updateMovieById(MovieDTO movieDTO, MultipartFile multipartFile) throws IOException;
    void deleteMovieById(Long id);
    List<MovieDTO> getAllMovies(Integer page, Integer size, String code, String name, Long genreId);
}
