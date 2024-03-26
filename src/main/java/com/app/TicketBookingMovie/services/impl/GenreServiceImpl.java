package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.GenreDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Genre;
import com.app.TicketBookingMovie.repository.GenreRepository;
import com.app.TicketBookingMovie.services.GenreService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreServiceImpl implements GenreService {
    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;

    public GenreServiceImpl(GenreRepository genreRepository, ModelMapper modelMapper) {
        this.genreRepository = genreRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createGenre(GenreDto genreDTO) {
        if (genreRepository.findByName(genreDTO.getName()).isPresent()) {
            throw new AppException("name: " + genreDTO.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        Genre genre = modelMapper.map(genreDTO, Genre.class);
        genre.setCode(randomCode());
        genre.setCreatedDate(LocalDateTime.now());
        genreRepository.save(genre);
    }

    @Override
    public GenreDto getGenreById(Long id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException("Genre not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(genre, GenreDto.class);
    }

    @Override
    public void updateGenreById(GenreDto genreDTO) {
        Genre genre = genreRepository.findById(genreDTO.getId()).orElseThrow(() -> new AppException("Genre not found with id: " + genreDTO.getId(), HttpStatus.NOT_FOUND));
        if(genreRepository.findByName(genreDTO.getName()).isPresent()){
            throw new AppException("name: " + genreDTO.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        if(!genreDTO.getName().isEmpty() && !genreDTO.getName().isBlank()) {
            genre.setName(genreDTO.getName());
        }else{
            genre.setName(genre.getName());
        }
        genreRepository.save(genre);
    }

    @Override
    public void deleteGenreById(Long id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException("Genre not found with id: " + id, HttpStatus.NOT_FOUND));
        genreRepository.delete(genre);
    }

    @Override
    public List<GenreDto> getAllGenre(Integer page, Integer size, String code, String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Genre> genres;
        if (code != null && !code.isEmpty()) {
            genres = genreRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            genres = genreRepository.findByNameContaining(name, pageable);
        } else {
            genres = genreRepository.findAll(pageable);
        }
        return genres.stream().map(genre -> modelMapper
                .map(genre, GenreDto.class)).collect(Collectors.toList());
    }

    @Override
    public long countAllGenres(String code, String name) {
        if (code != null && !code.isEmpty()) {
            return genreRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return genreRepository.countByNameContaining(name);
        } else {
            return genreRepository.count();
        }
    }

    public String randomCode() {
        return "LO"+ LocalDateTime.now().getNano();
    }
}
