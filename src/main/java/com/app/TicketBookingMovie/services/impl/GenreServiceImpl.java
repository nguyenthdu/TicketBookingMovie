package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.GenreDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Genre;
import com.app.TicketBookingMovie.repository.GenreRepository;
import com.app.TicketBookingMovie.services.GenreService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {
	@Autowired
	private GenreRepository genreRepository;
	@Autowired
	private final ModelMapper modelMapper;
	
	public GenreServiceImpl(GenreRepository genreRepository, ModelMapper modelMapper) {
		this.genreRepository = genreRepository;
		this.modelMapper = modelMapper;
	}
	
	@Override
	public GenreDTO createGenre(GenreDTO genreDTO) {
		if(genreRepository.findByName(genreDTO.getName()).isPresent()) {
			throw new AppException("name: " + genreDTO.getName() + " already exists", HttpStatus.BAD_REQUEST);
		}
		Genre genre = modelMapper.map(genreDTO, Genre.class);
		genre.setCode(randomCode());
		genreRepository.save(genre);
		return modelMapper.map(genre, GenreDTO.class);
	}
	
	@Override
	public GenreDTO getGenreById(Long id) {
		Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException("Genre not found with id: " + id, HttpStatus.NOT_FOUND));
		return modelMapper.map(genre, GenreDTO.class);
	}
	
	@Override
	public GenreDTO updateGenreById(GenreDTO genreDTO) {
		Genre genre = genreRepository.findById(genreDTO.getId()).orElseThrow(() -> new AppException("Genre not found with id: " + genreDTO.getId(), HttpStatus.NOT_FOUND));
		genre.setName(genreDTO.getName());
		genreRepository.save(genre);
		return modelMapper.map(genre, GenreDTO.class);
	}
	
	@Override
	public void deleteGenreById(Long id) {
		Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException("Genre not found with id: " + id, HttpStatus.NOT_FOUND));
		genreRepository.delete(genre);
	}
	
	@Override
	public List<GenreDTO> getAllGenre(Integer page, Integer size, Long code, String name) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Genre> genres;
		if(code != null) {
			genres = genreRepository.findByCode(code, pageable);
		} else if(name != null) {
			genres = genreRepository.findByNameContaining(name, pageable);
		} else {
			genres = genreRepository.findAll(pageable);
		}
		return genres.stream().map(genre -> modelMapper.map(genre, GenreDTO.class)).collect(Collectors.toList());
	}
	
	public Long randomCode() {
		//code là ngày thangs năm giờ phút giây
		return Long.parseLong(String.valueOf(System.currentTimeMillis()));
	}
}
