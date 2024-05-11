package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.AddressDto;
import com.app.TicketBookingMovie.dtos.CinemaDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.CinemaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("api/cinema")
public class CinemaController {
    private final CinemaService cinemaService;

    public CinemaController(CinemaService cinemaService) {
        this.cinemaService = cinemaService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createCinema(
            @RequestParam("name") String name,
            @RequestParam("status") boolean status,
            @RequestParam("street") String street,
            @RequestParam("ward") String ward,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        CinemaDto cinemaDto = new CinemaDto();
        cinemaDto.setName(name);
        cinemaDto.setStatus(status);
        AddressDto address = new AddressDto();
        address.setStreet(street);
        address.setWard(ward);
        address.setDistrict(district);
        address.setCity(city);
        address.setNation(nation);
        cinemaDto.setAddress(address);
        try{
            cinemaService.createCinema(cinemaDto);
            return  ResponseEntity.ok(new MessageResponse("Tạo rạp thành công.", HttpStatus.CREATED.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaDto> getCinemaById(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaService.getCinemaById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CinemaDto>> getAllCinema(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, name = "code") String code,
            @RequestParam(required = false,name = "name") String name,
            @RequestParam(required = false, name = "street") String street,
            @RequestParam(required = false, name = "ward") String ward,
            @RequestParam(required = false, name = "district") String district,
            @RequestParam(required = false, name = "city") String city,
            @RequestParam(required = false,name = "nation") String nation) {
        PageResponse<CinemaDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(cinemaService.getAllCinemas(page, size,code, name, street,ward, district, city, nation));
        pageResponse.setTotalElements(cinemaService.countAllCinemas(code, name, street, ward,district, city, nation));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok().body(pageResponse);

    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateCinema(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("status") boolean status,
            @RequestParam("street") String street,
            @RequestParam("ward") String ward,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        CinemaDto cinemaDto = new CinemaDto();
        cinemaDto.setId(id);
        cinemaDto.setName(name);
        cinemaDto.setStatus(status);
        AddressDto address = new AddressDto();
        address.setStreet(street);
        address.setWard(ward);
        address.setDistrict(district);
        address.setCity(city);
        address.setNation(nation);
        cinemaDto.setAddress(address);
        try {
            cinemaService.updateCinema(cinemaDto);
            return ResponseEntity.ok(new MessageResponse("Cập nhật rạp thành công.", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteCinemaById(@PathVariable Long id) {
        try {
            cinemaService.deleteCinemaById(id);
            return ResponseEntity.ok(new MessageResponse("Xóa rạp thành công.", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

}
