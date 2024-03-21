package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.GenreDto;
import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.repository.GenreRepository;
import com.app.TicketBookingMovie.services.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/genre")
public class GenreController {
    private final GenreService genreService;
    private final GenreRepository genreRepository;
    public GenreController(GenreService genreService, GenreRepository genreRepository) {
        this.genreService = genreService;
        this.genreRepository = genreRepository;
    }

    @PostMapping()
    public ResponseEntity<GenreDto> createGenre(@RequestParam("name") String name) {
        GenreDto genreDTO = new GenreDto();
        genreDTO.setName(name);
        return ResponseEntity.ok(genreService.createGenre(genreDTO));


    }

    @GetMapping("{id}")
    public ResponseEntity<GenreDto> getGenreById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponseDto> updateGenreById(@RequestParam("id") Long id, @RequestParam("name") String name) {
        GenreDto genreDTO = new GenreDto();
        genreDTO.setId(id);
        genreDTO.setName(name);
        try {
            genreService.updateGenreById(genreDTO);
            return ResponseEntity.ok().body(new MessageResponseDto("Genre updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponseDto> deleteGenreById(Long id) {
        try {
            genreService.deleteGenreById(id);
            return ResponseEntity.ok().body(new MessageResponseDto("Genre deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<GenreDto>> getAllGenres(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name) {
        PageResponse<GenreDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(genreService.getAllGenre(page, size, code, name));
        pageResponse.setTotalElements(genreService.countAllGenres(code, name));
        pageResponse.setTotalPages((int) Math.ceil((double)  pageResponse.getTotalElements()/ size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok().body(pageResponse);
    }
}
