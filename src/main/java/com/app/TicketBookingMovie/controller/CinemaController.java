package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.AddressDto;
import com.app.TicketBookingMovie.dtos.CinemaDto;
import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.CinemaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/cinema")
public class CinemaController {
    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @PostMapping
    public ResponseEntity<CinemaDto> createCinema(
            @RequestParam("name") String name,
            @RequestParam("totalRoom") int totalRoom,
            @RequestParam("status") boolean status,
            @RequestParam("street") String street,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        CinemaDto cinemaDto = new CinemaDto();
        cinemaDto.setName(name);
        cinemaDto.setTotalRoom(totalRoom);
        cinemaDto.setStatus(status);
        AddressDto address = new AddressDto();
        address.setStreet(street);
        address.setDistrict(district);
        address.setCity(city);
        address.setNation(nation);
        cinemaDto.setAddress(address);
        return ResponseEntity.ok(cinemaService.createCinema(cinemaDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaDto> getCinemaById(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CinemaDto>> getAllCinema(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "2") Integer size,
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("street") String street,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        PageResponse<CinemaDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(cinemaService.getAllCinemas(page, size,code, name, street, district, city, nation));
        pageResponse.setTotalElements(pageResponse.getContent().size());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);

    }

    @PutMapping
    public ResponseEntity<MessageResponseDTO> updateCinema(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("totalRoom") int totalRoom,
            @RequestParam("status") boolean status,
            @RequestParam("street") String street,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        CinemaDto cinemaDto = new CinemaDto();
        cinemaDto.setId(id);
        cinemaDto.setName(name);
        cinemaDto.setTotalRoom(totalRoom);
        cinemaDto.setStatus(status);
        AddressDto address = new AddressDto();
        address.setStreet(street);
        address.setDistrict(district);
        address.setCity(city);
        address.setNation(nation);
        cinemaDto.setAddress(address);
        try {
            cinemaService.updateCinema(cinemaDto);
            return ResponseEntity.ok(new MessageResponseDTO("Update cinema successfully with id:" + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteCinemaById(@PathVariable Long id) {
        try {
            cinemaService.deleteCinemaById(id);
            return ResponseEntity.ok(new MessageResponseDTO("Delete cinema successfully with id:" + id, HttpStatus.NO_CONTENT.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponseDTO(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

}
