package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.dtos.RevenueByCinemaDto;
import com.app.TicketBookingMovie.dtos.RevenueByMovieDto;
import com.app.TicketBookingMovie.dtos.RevenueByUserDto;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.repository.InvoiceRepository;
import com.app.TicketBookingMovie.repository.ReturnInvoiceRepository;
import com.app.TicketBookingMovie.services.*;
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
    public List<RevenueByCinemaDto> getRevenueByCinema(Integer page, Integer size, String cinemaCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
        List<Invoice> pageInvoices = invoiceRepository.findAll().stream().filter(Invoice::isStatus).collect(Collectors.toList());
        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!cinemaCode.isEmpty() && !cinemaCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getCode().equals(cinemaCode)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getCreatedDate().isAfter(startDate.atStartOfDay())
                    && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé và tổng doanh thu
        List<RevenueByCinemaDto> revenueByCinemaDtos = pageInvoices.stream().collect(Collectors.groupingBy(invoice ->
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
        // Sắp xếp kết quả nếu sort type là totalRevenue thì sort theo tổng tiền còn nếu là name thì sort theo tên
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByCinemaDtos.sort(Comparator.comparing(RevenueByCinemaDto::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByCinemaDtos.sort(Comparator.comparing(RevenueByCinemaDto::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByCinemaDtos.sort(Comparator.comparing(RevenueByCinemaDto::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByCinemaDtos.sort(Comparator.comparing(RevenueByCinemaDto::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, revenueByCinemaDtos.size());
        return revenueByCinemaDtos.subList(fromIndex, toIndex);
    }

    //TODO: Thống kê doanh thu theo phim
    @Override
    public List<RevenueByMovieDto> getRevenueByMovie(Integer page, Integer size, String movieCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
        List<Invoice> pageInvoices = invoiceRepository.findAll().stream().filter(Invoice::isStatus).collect(Collectors.toList());
        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!movieCode.isEmpty() && !movieCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCode().equals(movieCode)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getCreatedDate().isAfter(startDate.atStartOfDay())
                    && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé và tổng doanh thu
        List<RevenueByMovieDto> revenueByMovieDtos = pageInvoices.stream().collect(Collectors.groupingBy(invoice ->
                        invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getId()))
                .entrySet().stream()
                .map(entry -> {
                    RevenueByMovieDto revenueByMovieDto = new RevenueByMovieDto();
                    Movie movie = movieService.findById(entry.getKey()); // Lấy thông tin phim từ cơ sở dữ liệu
                    revenueByMovieDto.setCode(movie.getCode());
                    revenueByMovieDto.setName(movie.getName());
                    revenueByMovieDto.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của phim
                    revenueByMovieDto.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của phim
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của phim
                    revenueByMovieDto.setTotalRevenue(totalRevenue);
                    return revenueByMovieDto;
                }).collect(Collectors.toList());

        // Sắp xếp kết quả
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByMovieDtos.sort(Comparator.comparing(RevenueByMovieDto::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByMovieDtos.sort(Comparator.comparing(RevenueByMovieDto::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByMovieDtos.sort(Comparator.comparing(RevenueByMovieDto::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByMovieDtos.sort(Comparator.comparing(RevenueByMovieDto::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, revenueByMovieDtos.size());
        return revenueByMovieDtos.subList(fromIndex, toIndex);

    }

    @Override
    public List<RevenueByUserDto> getRevenueByUser(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {

        List<Invoice> pageInvoices = invoiceRepository.findAll().stream().filter(Invoice::isStatus).collect(Collectors.toList());

        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!userCode.isEmpty() && !userCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getUser().getCode().equals(userCode)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (!email.isEmpty() && !email.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getUser().getEmail().equals(email)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (!phone.isEmpty() && !phone.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getUser().getPhone().equals(phone)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getCreatedDate().isAfter(startDate.atStartOfDay())
                    && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé, tổng giảm giá và tổng doanh thu
        List<RevenueByUserDto> revenueByUserDtos = pageInvoices.stream().collect(Collectors.groupingBy(invoice -> invoice.getUser().getId()))
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
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, revenueByUserDtos.size());
        return revenueByUserDtos.subList(fromIndex, toIndex);

    }

    @Override
    public List<RevenueByUserDto> getRevenueByStaff(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
        List<Invoice> pageInvoices = invoiceRepository.findAll().stream().filter(Invoice::isStatus).collect(Collectors.toList());
        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!userCode.isEmpty() && !userCode.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getStaff().getCode().equals(userCode)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (!email.isEmpty() && !email.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getStaff().getEmail().equals(email)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (!phone.isEmpty() && !phone.isBlank() && startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getStaff().getPhone().equals(phone)
                    && invoice.getCreatedDate().isAfter(startDate.atStartOfDay()) && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pageInvoices = pageInvoices.stream().filter(invoice -> invoice.getCreatedDate().isAfter(startDate.atStartOfDay())
                    && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé, tổng giảm giá và tổng doanh thu
        List<RevenueByUserDto> revenueByUserDtos = pageInvoices.stream().filter(invoice -> invoice.getStaff() != null)
                .collect(Collectors.groupingBy(invoice -> invoice.getStaff().getId()))
                .entrySet().stream()
                .map(entry -> {
                    RevenueByUserDto revenueByUserDto = new RevenueByUserDto();
                    User user = userService.findById(entry.getKey()); // Lấy thông tin nhân viên từ cơ sở dữ liệu
                    revenueByUserDto.setCode(user.getCode());
                    revenueByUserDto.setName(user.getUsername());
                    revenueByUserDto.setEmail(user.getEmail());
                    revenueByUserDto.setPhone(user.getPhone());
                    revenueByUserDto.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của nhân viên
                    revenueByUserDto.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của nhân viên
//                    BigDecimal totalDiscount = entry.getValue().stream().map(Invoice::getPromotionLines).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng giảm giá từ tổng giá trị của các hóa đơn của nhân viên
//                    revenueByUserDto.setTotalDiscount(totalDiscount);
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của nhân viên
                    revenueByUserDto.setTotalRevenue(totalRevenue);
                    return revenueByUserDto;
                }).collect(Collectors.toList());
        // Sắp xếp kết quả
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                revenueByUserDtos.sort(Comparator.comparing(RevenueByUserDto::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, revenueByUserDtos.size());
        return revenueByUserDtos.subList(fromIndex, toIndex);

    }

    @Override
    public List<ReturnInvoiceDto> getReturnInvoice(Integer page, Integer size, String code, String userCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
        List<ReturnInvoice> pageReturnInvoice = returnInvoiceRepository.findAll();
        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!code.isEmpty() && !code.isBlank() && startDate != null && endDate != null) {
            pageReturnInvoice = pageReturnInvoice.stream().filter(returnInvoice -> returnInvoice.getCode().equals(code)
                    && returnInvoice.getCancelDate().isAfter(startDate.atStartOfDay()) && returnInvoice.getCancelDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (!userCode.isEmpty() && !userCode.isBlank() && startDate != null && endDate != null) {
            pageReturnInvoice = pageReturnInvoice.stream().filter(returnInvoice -> returnInvoice.getInvoice().getUser().getCode().equals(userCode)
                    && returnInvoice.getCancelDate().isAfter(startDate.atStartOfDay()) && returnInvoice.getCancelDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pageReturnInvoice = pageReturnInvoice.stream().filter(returnInvoice -> returnInvoice.getCancelDate().isAfter(startDate.atStartOfDay())
                    && returnInvoice.getCancelDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        // Lọc ra các hóa đơn hủy
        List<ReturnInvoiceDto> returnInvoiceDtos = pageReturnInvoice.stream().map(returnInvoice -> {
            ReturnInvoiceDto returnInvoiceDto = new ReturnInvoiceDto();
            returnInvoiceDto.setCode(returnInvoice.getCode());
            returnInvoiceDto.setCancelDate(returnInvoice.getCancelDate());
            returnInvoiceDto.setTotal(returnInvoice.getInvoice().getTotalPrice());
            returnInvoiceDto.setReason(returnInvoice.getReason());
            returnInvoiceDto.setInvoiceId(returnInvoice.getInvoice().getId());
            returnInvoiceDto.setInvoiceCode(returnInvoice.getInvoice().getCode());
            returnInvoiceDto.setInvoiceDate(returnInvoice.getInvoice().getCreatedDate());
            returnInvoiceDto.setUserCode(returnInvoice.getInvoice().getUser().getCode());
            returnInvoiceDto.setUserName(returnInvoice.getInvoice().getUser().getUsername());
            //lấy số lượng sản phẩm trong hóa đơn
            returnInvoiceDto.setQuantity(returnInvoice.getInvoice().getInvoiceTicketDetails().size() + returnInvoice.getInvoice().getInvoiceFoodDetails().size());
            return returnInvoiceDto;
        }).collect(Collectors.toList());
        // Sắp xếp kết quả loại sort sẽ là theo ngày và theo tổng tiền
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                returnInvoiceDtos.sort(Comparator.comparing(ReturnInvoiceDto::getTotal));
            } else if (sortDirection == Sort.Direction.DESC) {
                returnInvoiceDtos.sort(Comparator.comparing(ReturnInvoiceDto::getTotal).reversed());
            }
        } else if (sortType.equalsIgnoreCase("date")) {
            if (sortDirection == Sort.Direction.ASC) {
                returnInvoiceDtos.sort(Comparator.comparing(ReturnInvoiceDto::getCancelDate));
            } else if (sortDirection == Sort.Direction.DESC) {
                returnInvoiceDtos.sort(Comparator.comparing(ReturnInvoiceDto::getCancelDate).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, returnInvoiceDtos.size());
        return returnInvoiceDtos.subList(fromIndex, toIndex);

    }

}
