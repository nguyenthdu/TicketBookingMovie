package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.GenreDto;

import java.util.List;

public interface GenreService {
	GenreDto createGenre(GenreDto genreDTO);
	GenreDto getGenreById(Long id);
	GenreDto updateGenreById(GenreDto genreDTO);
	void deleteGenreById(Long id);
	List<GenreDto> getAllGenre(Integer page, Integer size, String code, String name);

    long countAllGenres(String code, String name);
}
