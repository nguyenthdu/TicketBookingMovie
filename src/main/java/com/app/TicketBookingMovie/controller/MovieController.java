package com.app.TicketBookingMovie.controller;


import com.app.TicketBookingMovie.dtos.MovieDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.MovieService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/api/movie")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createMovie(
            @RequestParam("name") String name,
            @RequestParam("image") String image,
            @RequestParam("trailer") String trailer,
            @RequestParam("description") String description,
            @RequestParam("duration") int duration,
            @RequestParam("genreId") Set<Long> genreId,
            @RequestParam("releaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
            @RequestParam("country") String country,
            @RequestParam("director") String director,
            @RequestParam("cast") String cast,
            @RequestParam("producer") String producer,
            @RequestParam("cinemaId") Set<Long> cinemaId,
            @RequestParam("status") boolean status) {
        MovieDto movieDTO = new MovieDto();
        movieDTO.setName(name);
        movieDTO.setImageLink(image);
        movieDTO.setTrailerLink(trailer);
        movieDTO.setDescription(description);
        movieDTO.setDurationMinutes(duration);
        movieDTO.setGenreIds(genreId);
        movieDTO.setReleaseDate(releaseDate);
        movieDTO.setCountry(country);
        movieDTO.setDirector(director);
        movieDTO.setCast(cast);
        movieDTO.setProducer(producer);
        movieDTO.setCinemaIds(cinemaId);
        movieDTO.setStatus(status);
        try {
            movieService.createMovie(movieDTO);
            return ResponseEntity.ok().body(new MessageResponse("Tạo phim thành công.", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    //fidn by id
    @GetMapping("/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable("id") Long id) {
        MovieDto movieDTO = movieService.getMovieById(id);
        return ResponseEntity.ok(movieDTO);
    }

    //update
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateMovie(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("image") String image,
            @RequestParam("trailer") String trailer,
            @RequestParam("description") String description,
            @RequestParam("duration") int duration,
            @RequestParam("genreId") Set<Long> genreId,
            @RequestParam("releaseDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
            @RequestParam("country") String country,
            @RequestParam("director") String director,
            @RequestParam("cast") String cast,
            @RequestParam("producer") String producer,
            @RequestParam("cinemaId") Set<Long> cinemaId,
            @RequestParam("status") boolean status) {
        MovieDto movieDTO = new MovieDto();
        movieDTO.setId(id);
        movieDTO.setName(name);
        movieDTO.setImageLink(image);
        movieDTO.setTrailerLink(trailer);
        movieDTO.setDescription(description);
        movieDTO.setDurationMinutes(duration);
        movieDTO.setGenreIds(genreId);
        movieDTO.setReleaseDate(releaseDate);
        movieDTO.setCountry(country);
        movieDTO.setDirector(director);
        movieDTO.setCast(cast);
        movieDTO.setProducer(producer);
        movieDTO.setCinemaIds(cinemaId);
        movieDTO.setStatus(status);
        try {
            movieService.updateMovieById(movieDTO);
            return ResponseEntity.ok().body(new MessageResponse("Cập nhật phim thành công.", HttpStatus.OK.value(), Instant.now().toString()));

        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    //delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteMovie(@PathVariable("id") Long id) {
        try {
            movieService.deleteMovieById(id);
            return ResponseEntity.ok().body(new MessageResponse("Xóa phim thành công.", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }

    }
    @GetMapping("/upcoming")
    public ResponseEntity<PageResponse<MovieDto>> getMoviesNotShowed(

            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResponse<MovieDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(movieService.getMoviesUpcoming(page, size));
        pageResponse.setTotalElements(movieService.getMoviesUpcoming(page, size).size());
        pageResponse.setTotalPages(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
    @GetMapping("/showing")
    public ResponseEntity<PageResponse<MovieDto>> getMoviesShowing(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        PageResponse<MovieDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(movieService.getMoviesShowing(page, size));
        pageResponse.setTotalElements(movieService.getMoviesShowing(page, size).size());
        pageResponse.setTotalPages(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping
    public ResponseEntity<PageResponse<MovieDto>> getAllMovies(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Long cinemaId) {
        PageResponse<MovieDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(movieService.getAllMovies(page, size, code, name, genreId, cinemaId));
        pageResponse.setTotalElements(movieService.countAllMovies(code, name, genreId, cinemaId));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
}
