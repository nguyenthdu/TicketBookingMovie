package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.GenreDTO;
import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.repository.GenreRepository;
import com.app.TicketBookingMovie.services.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/genre")
@RequiredArgsConstructor
public class GenreController {
	@Autowired
	private final GenreService genreService;
	@Autowired
	private final GenreRepository genreRepository;
	
	@PostMapping()
	public ResponseEntity<GenreDTO> createGenre(@RequestParam("name") String name) {
		GenreDTO genreDTO = new GenreDTO();
		genreDTO.setName(name);
		return ResponseEntity.ok(genreService.createGenre(genreDTO));
	}
	
	@GetMapping
	public ResponseEntity<GenreDTO> getGenreById(@RequestParam("id") Long id) {
		return ResponseEntity.ok(genreService.getGenreById(id));
	}
	
	@PutMapping
	public ResponseEntity<MessageResponseDTO> updateGenreById(@RequestParam("id") Long id, @RequestParam("name") String name) {
		GenreDTO genreDTO = new GenreDTO();
		genreDTO.setId(id);
		genreDTO.setName(name);
		try {
			genreService.updateGenreById(genreDTO);
			return ResponseEntity.ok().body(new MessageResponseDTO("Genre updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
		} catch (AppException e) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
		}
	}
	
	@DeleteMapping
	public ResponseEntity<MessageResponseDTO> deleteGenreById(Long id) {
		try {
			genreService.deleteGenreById(id);
			return ResponseEntity.ok().body(new MessageResponseDTO("Genre deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
		} catch (AppException e) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
		}
	}
	
	@GetMapping("/all")
	public ResponseEntity<PageResponse<GenreDTO>> getAllGenres(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "2") Integer size, @RequestParam(required = false) Long code, @RequestParam(required = false) String name) {
		PageResponse<GenreDTO> genrePageResponse = new PageResponse<>();
		genrePageResponse.setContent(genreService.getAllGenre(page, size, code, name));
		genrePageResponse.setTotalElements(genreRepository.count());
		genrePageResponse.setTotalPages((int) Math.ceil((double) genreRepository.count() / size));
		genrePageResponse.setCurrentPage(page);
		genrePageResponse.setPageSize(size);
		return ResponseEntity.ok().body(genrePageResponse);
	}
}
