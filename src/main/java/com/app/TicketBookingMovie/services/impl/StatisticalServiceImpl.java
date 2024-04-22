package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.dtos.RevenueByCinemaDto;
import com.app.TicketBookingMovie.dtos.RevenueByMovieDto;
import com.app.TicketBookingMovie.dtos.RevenueByUserDto;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.repository.InvoiceRepository;
import com.app.TicketBookingMovie.repository.ReturnInvoiceRepository;
import com.app.TicketBookingMovie.services.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticalServiceImpl implements StatisticalService {
    private final InvoiceService invoiceService;
    private final UserService userService;
    private final CinemaService cinemaService;
    private final MovieService movieService;
    private final InvoiceRepository invoiceRepository;
    private final PromotionLineService promotionLineService;
    private final ReturnInvoiceRepository returnInvoiceRepository;

    public StatisticalServiceImpl(InvoiceService invoiceService, UserService userService, CinemaService cinemaService, MovieService movieService, InvoiceRepository invoiceRepository, PromotionLineService promotionLineService, ReturnInvoiceRepository returnInvoiceRepository) {
        this.invoiceService = invoiceService;
        this.userService = userService;
        this.cinemaService = cinemaService;
        this.movieService = movieService;
        this.invoiceRepository = invoiceRepository;
        this.promotionLineService = promotionLineService;
        this.returnInvoiceRepository = returnInvoiceRepository;
    }

    //TODO: Thống kê doanh thu theo rạp
    @Override
    public List<RevenueByCinemaDto> getRevenueByCinema(Integer page, Integer size, String cinemaCode, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Invoice> pageInvoices;

        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!cinemaCode.isEmpty() && !cinemaCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByCinemaCodeAndCreatedDateBetween(cinemaCode, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByCreatedDateBetween(startDate, endDate, pageable);
        } else {
            pageInvoices = invoiceRepository.findAll(pageable);
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé và tổng doanh thu
        List<RevenueByCinemaDto> revenueByCinemaDtos = pageInvoices.getContent().stream().collect(Collectors.groupingBy(invoice ->
                        invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getId()))
                .entrySet().stream()
                .map(entry -> {
                    RevenueByCinemaDto revenueByCinemaDto = new RevenueByCinemaDto();
                    Cinema cinema = cinemaService.findById(entry.getKey()); // Lấy thông tin rạp từ cơ sở dữ liệu
                    revenueByCinemaDto.setCode(cinema.getCode());
                    revenueByCinemaDto.setName(cinema.getName());
                    Address address = cinema.getAddress();
                    String addressString = address.getStreet() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getCity();
                    revenueByCinemaDto.setAddress(addressString);
                    revenueByCinemaDto.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của rạp
                    revenueByCinemaDto.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của rạp
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của rạp
                    revenueByCinemaDto.setTotalRevenue(totalRevenue);
                    return revenueByCinemaDto;
                }).collect(Collectors.toList());

        // Sắp xếp kết quả
        if (sortDirection == Sort.Direction.ASC) {
            revenueByCinemaDtos.sort(Comparator.comparing(RevenueByCinemaDto::getTotalRevenue));
        } else if (sortDirection == Sort.Direction.DESC) {
            revenueByCinemaDtos.sort(Comparator.comparing(RevenueByCinemaDto::getTotalRevenue).reversed());
        }

        return revenueByCinemaDtos;
    }

    //TODO: Thống kê doanh thu theo phim
    @Override
    public List<RevenueByMovieDto> getRevenueByMovie(Integer page, Integer size, String movieCode, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Invoice> pageInvoices;

        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!movieCode.isEmpty() && !movieCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByMovieCodeAndCreatedDateBetween(movieCode, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByCreatedDateBetween(startDate, endDate, pageable);
        } else {
            pageInvoices = invoiceRepository.findAll(pageable);
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé và tổng doanh thu
        List<RevenueByMovieDto> revenueByMovieDtos = pageInvoices.getContent().stream().collect(Collectors.groupingBy(invoice ->
                        invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getId()))
                .entrySet().stream()
                .map(entry -> {
                    RevenueByMovieDto revenueByMovieDto = new RevenueByMovieDto();
                    Movie movie = movieService.findById(entry.getKey()); // Lấy thông tin phim từ cơ sở dữ liệu
                    revenueByMovieDto.setCode(movie.getCode());
                    revenueByMovieDto.setName(movie.getName());
                    revenueByMovieDto.setImage(movie.getImageLink());
                    revenueByMovieDto.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của phim
                    revenueByMovieDto.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của phim
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của phim
                    revenueByMovieDto.setTotalRevenue(totalRevenue);
                    return revenueByMovieDto;
                }).collect(Collectors.toList());

        // Sắp xếp kết quả
        if (sortDirection == Sort.Direction.ASC) {
            revenueByMovieDtos.sort(Comparator.comparing(RevenueByMovieDto::getTotalRevenue));
        } else if (sortDirection == Sort.Direction.DESC) {
            revenueByMovieDtos.sort(Comparator.comparing(RevenueByMovieDto::getTotalRevenue).reversed());
        }


        return revenueByMovieDtos;
    }

    @Override
    public List<RevenueByUserDto> getRevenueByUser(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Invoice> pageInvoices;

        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!userCode.isEmpty() && !userCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByUserIdAndCreatedDateBetween(userCode, startDate, endDate, pageable);
        } else if (!email.isEmpty() && !email.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByUserEmailAndCreatedDateBetween(email, startDate, endDate, pageable);
        } else if (!phone.isEmpty() && !phone.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByUserPhoneAndCreatedDateBetween(phone, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByCreatedDateBetween(startDate, endDate, pageable);
        } else {
            pageInvoices = invoiceRepository.findAll(pageable);
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé, tổng giảm giá và tổng doanh thu
        List<RevenueByUserDto> revenueByUserDtos = pageInvoices.getContent().stream().collect(Collectors.groupingBy(invoice -> invoice.getUser().getId()))
                .entrySet().stream()
                .map(entry -> {
                    RevenueByUserDto revenueByUserDto = new RevenueByUserDto();
                    User user = userService.findById(entry.getKey()); // Lấy thông tin người dùng từ cơ sở dữ liệu
                    revenueByUserDto.setCode(user.getCode());
                    revenueByUserDto.setName(user.getUsername());
                    revenueByUserDto.setEmail(user.getEmail());
                    revenueByUserDto.setPhone(user.getPhone());
                    revenueByUserDto.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của người dùng
                    revenueByUserDto.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của người dùng
//                    BigDecimal totalDiscount = entry.getValue().stream().map(Invoice::getPromotionLines).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng giảm giá từ tổng giá trị của các hóa đơn của người dùng
//                    revenueByUserDto.setTotalDiscount(totalDiscount);
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của người dùng
                    revenueByUserDto.setTotalRevenue(totalRevenue);
                    return revenueByUserDto;
                }).collect(Collectors.toList());

        // Sắp xếp kết quả
        if (sortDirection == Sort.Direction.ASC) {
            revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue));
        } else if (sortDirection == Sort.Direction.DESC) {
            revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue).reversed());
        }
        return revenueByUserDtos;
    }

    @Override
    public List<RevenueByUserDto> getRevenueByStaff(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Invoice> pageInvoices;

        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!userCode.isEmpty() && !userCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByUserIdAndCreatedDateBetween(userCode, startDate, endDate, pageable);
        } else if (!email.isEmpty() && !email.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByUserEmailAndCreatedDateBetween(email, startDate, endDate, pageable);
        } else if (!phone.isEmpty() && !phone.isBlank() && startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByUserPhoneAndCreatedDateBetween(phone, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            pageInvoices = invoiceRepository.findAllByCreatedDateBetween(startDate, endDate, pageable);
        } else {
            pageInvoices = invoiceRepository.findAll(pageable);
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé, tổng giảm giá và tổng doanh thu
        List<RevenueByUserDto> revenueByUserDtos = pageInvoices.getContent().stream().collect(Collectors.groupingBy(invoice -> invoice.getStaff().getId()))
                .entrySet().stream()
                .map(entry -> {
                    RevenueByUserDto revenueByUserDto = new RevenueByUserDto();
                    User user = userService.findById(entry.getKey()); // Lấy thông tin người dùng từ cơ sở dữ liệu
                    revenueByUserDto.setCode(user.getCode());
                    revenueByUserDto.setName(user.getUsername());
                    revenueByUserDto.setEmail(user.getEmail());
                    revenueByUserDto.setPhone(user.getPhone());
                    revenueByUserDto.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của người dùng
                    revenueByUserDto.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của người dùng
//                    BigDecimal totalDiscount = entry.getValue().stream().map(Invoice::getPromotionLines).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng giảm giá từ tổng giá trị của các hóa đơn của người dùng
//                    revenueByUserDto.setTotalDiscount(totalDiscount);
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của người dùng
                    revenueByUserDto.setTotalRevenue(totalRevenue);
                    return revenueByUserDto;
                }).collect(Collectors.toList());

        // Sắp xếp kết quả
        if (sortDirection == Sort.Direction.ASC) {
            revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue));
        } else if (sortDirection == Sort.Direction.DESC) {
            revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue).reversed());
        }
        return revenueByUserDtos;
    }

    @Override
    public List<ReturnInvoiceDto> getReturnInvoice(Integer page, Integer size, String code, String userCode, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ReturnInvoice> pageInvoices;

        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!code.isEmpty() && !code.isBlank() && startDate != null && endDate != null) {
            pageInvoices = returnInvoiceRepository.findAllByCodeAndCancelDateBetween(code, startDate, endDate, pageable);
        } else if (!userCode.isEmpty() && !userCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = returnInvoiceRepository.findAllByUserCodeAndReturnDateBetween(userCode, startDate, endDate, pageable);
        } else if (startDate != null && endDate != null) {
            pageInvoices = returnInvoiceRepository.findAllByReturnDateBetween(startDate, endDate, pageable);
        } else {
            pageInvoices = returnInvoiceRepository.findAll(pageable);
        }
        // Lọc ra các hóa đơn hủy
        List<ReturnInvoiceDto> returnInvoiceDtos = pageInvoices.getContent().stream().collect(Collectors.groupingBy(ReturnInvoice::getInvoice))
                .entrySet().stream()
                .map(entry -> {
                    ReturnInvoiceDto returnInvoiceDto = new ReturnInvoiceDto();
                    returnInvoiceDto.setCode(entry.getValue().get(0).getCode());
                    returnInvoiceDto.setReason(entry.getValue().get(0).getReason());
                    returnInvoiceDto.setCancelDate(entry.getValue().get(0).getCancelDate());
                    returnInvoiceDto.setInvoiceId(entry.getValue().get(0).getInvoice().getId());
                    returnInvoiceDto.setInvoiceCode(entry.getValue().get(0).getInvoice().getCode());
                    returnInvoiceDto.setInvoiceDate(entry.getValue().get(0).getInvoice().getCreatedDate());
                    returnInvoiceDto.setUserCode(entry.getValue().get(0).getInvoice().getUser().getCode());
                    returnInvoiceDto.setUserName(entry.getValue().get(0).getInvoice().getUser().getUsername());
                    returnInvoiceDto.setQuantity(entry.getValue().size());
                    BigDecimal total = entry.getValue().stream().map(ReturnInvoice::getInvoice).map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                    returnInvoiceDto.setTotal(total);
                    return returnInvoiceDto;
                }).collect(Collectors.toList()
                );

        // Sắp xếp kết quả
        if (sortDirection == Sort.Direction.ASC) {
            returnInvoiceDtos.sort(Comparator.comparing(ReturnInvoiceDto::getTotal));
        } else if (sortDirection == Sort.Direction.DESC) {
            returnInvoiceDtos.sort(Comparator.comparing(ReturnInvoiceDto::getTotal).reversed());
        }

        return returnInvoiceDtos;
    }

}
