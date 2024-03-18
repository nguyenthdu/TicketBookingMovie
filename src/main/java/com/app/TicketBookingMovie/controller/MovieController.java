package com.app.TicketBookingMovie.controller;


import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.app.TicketBookingMovie.dtos.MovieDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.repository.MovieRepository;
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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/movie")
public class MovieController {
    private final MovieService movieService;
    private final MovieRepository movieRepository;


    public MovieController(MovieService movieService, MovieRepository movieRepository) {
        this.movieService = movieService;
        this.movieRepository = movieRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MovieDto>> getAllMovies(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "2") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long genreId) {
        PageResponse<MovieDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(movieService.getAllMovies(page, size, code, name, genreId));
        pageResponse.setTotalElements(movieRepository.count());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @PostMapping
    public ResponseEntity<MessageResponseDTO> createMovie(
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
            @RequestParam("producer") String producer) {
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
        try {
            movieService.createMovie(movieDTO, image);
            return ResponseEntity.ok().body(new MessageResponseDTO("Movie created successfully with movie name: " + name, HttpStatus.CREATED.value(), Instant.now().toString()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
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
    public ResponseEntity<MessageResponseDTO> updateMovie(
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
            @RequestParam("producer") String producer) {

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
        try {
            movieService.updateMovieById(movieDTO, image);
            return ResponseEntity.ok().body(new MessageResponseDTO("Movie updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    //delete
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteMovie(@PathVariable("id") Long id) {
        try {
            movieService.deleteMovieById(id);
            return ResponseEntity.ok().body(new MessageResponseDTO("Movie deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }

    }
    //get all

}
