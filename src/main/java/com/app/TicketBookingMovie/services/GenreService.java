package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.GenreDTO;

import java.util.List;

public interface GenreService {
	GenreDTO createGenre(GenreDTO genreDTO);
	GenreDTO getGenreById(Long id);
	GenreDTO updateGenreById( GenreDTO genreDTO);
	void deleteGenreById(Long id);
	List<GenreDTO> getAllGenre(Integer page, Integer size,  String code, String name);

}
