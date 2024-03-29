package com.app.TicketBookingMovie.controller;


import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.MovieDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.MovieService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PostMapping
    public ResponseEntity<MessageResponseDto> createMovie(
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image,
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
            movieService.createMovie(movieDTO, image);
            return ResponseEntity.ok().body(new MessageResponseDto("Movie created successfully with movie name: " + name, HttpStatus.CREATED.value(), Instant.now().toString()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
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
    public ResponseEntity<MessageResponseDto> updateMovie(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image,
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
            movieService.updateMovieById(movieDTO, image);
            return ResponseEntity.ok().body(new MessageResponseDto("Movie updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    //delete
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteMovie(@PathVariable("id") Long id) {
        try {
            movieService.deleteMovieById(id);
            return ResponseEntity.ok().body(new MessageResponseDto("Movie deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }

    }
    //get all

}
