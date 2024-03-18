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

import java.util.List;
import java.util.Random;
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
    public GenreDto createGenre(GenreDto genreDTO) {
        if (genreRepository.findByName(genreDTO.getName()).isPresent()) {
            throw new AppException("name: " + genreDTO.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        Genre genre = modelMapper.map(genreDTO, Genre.class);
        genre.setCode(randomCode());
        genreRepository.save(genre);
        return modelMapper.map(genre, GenreDto.class);
    }

    @Override
    public GenreDto getGenreById(Long id) {
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException("Genre not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(genre, GenreDto.class);
    }

    @Override
    public GenreDto updateGenreById(GenreDto genreDTO) {
        Genre genre = genreRepository.findById(genreDTO.getId()).orElseThrow(() -> new AppException("Genre not found with id: " + genreDTO.getId(), HttpStatus.NOT_FOUND));
        if(genreRepository.findByName(genreDTO.getName()).isPresent()){
            throw new AppException("name: " + genreDTO.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }

        genre.setName(genreDTO.getName());
        genreRepository.save(genre);
        return modelMapper.map(genre, GenreDto.class);
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

    public String randomCode() {
        Random random = new Random();
        String code;
        int number = random.nextInt(1000);
        code = "TL" + System.currentTimeMillis() + number;
        return code;
    }
}
