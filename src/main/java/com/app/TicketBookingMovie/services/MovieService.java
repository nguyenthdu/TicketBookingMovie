package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.MovieDto;
import com.app.TicketBookingMovie.models.Movie;

import java.util.List;

public interface MovieService {
    void createMovie(MovieDto movieDTO);
    MovieDto getMovieById(Long id);
    Movie findById(Long id);
    void updateMovieById(MovieDto movieDTO);
    void deleteMovieById(Long id);
    List<MovieDto> getAllMovies(Integer page, Integer size, String code, String name, Long genreId, Long cinemaId);

    long countAllMovies(String code, String name, Long genreId, Long cinemaId);
//    lấy danh sách phim chưa chiếu:
    List<MovieDto> getMoviesUpcoming(Integer page, Integer size);
    //lấy danh sách phim đang chiếu:
    List<MovieDto> getMoviesShowing(Integer page, Integer size);
}
