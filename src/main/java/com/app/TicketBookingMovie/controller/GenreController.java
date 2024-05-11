package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.GenreDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/genre")
public class GenreController {
    private final GenreService genreService;
    public GenreController(GenreService genreService) {
        this.genreService = genreService;

    }

    @PostMapping()
    public ResponseEntity<MessageResponse> createGenre(@RequestParam("name") String name) {
        GenreDto genreDTO = new GenreDto();
        genreDTO.setName(name);
        try {
            genreService.createGenre(genreDTO);
            return ResponseEntity.ok().body(new MessageResponse("Tạo thể loại phim thành công.", HttpStatus.CREATED.value(), Instant.now().toString()));
        }catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }

    }

    @GetMapping("{id}")
    public ResponseEntity<GenreDto> getGenreById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateGenreById(@RequestParam("id") Long id, @RequestParam("name") String name) {
        GenreDto genreDTO = new GenreDto();
        genreDTO.setId(id);
        genreDTO.setName(name);
        try {
            genreService.updateGenreById(genreDTO);
            return ResponseEntity.ok().body(new MessageResponse("Cập nhật thể lọai phim thành công.", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<MessageResponse> deleteGenreById(@PathVariable Long id) {
        try {
            genreService.deleteGenreById(id);
            return ResponseEntity.ok().body(new MessageResponse("Xoá thể loại phim thành công.", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
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
