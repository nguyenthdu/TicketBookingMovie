package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByCinema;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByMovie;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByUser;
import com.app.TicketBookingMovie.payload.response.ResponseRevenuePromotionLine;
import com.app.TicketBookingMovie.repository.InvoiceRepository;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.repository.ReturnInvoiceRepository;
import com.app.TicketBookingMovie.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticalServiceImpl implements StatisticalService {
    @Autowired
    private final InvoiceService invoiceService;
    private final UserService userService;
    private final CinemaService cinemaService;
    private final MovieService movieService;
    private final InvoiceRepository invoiceRepository;
    private final PromotionLineService promotionLineService;
    private final ReturnInvoiceRepository returnInvoiceRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final FoodService foodService;
    private final TicketService ticketService;
    private final TypeSeatService typeSeatService;


    public StatisticalServiceImpl(InvoiceService invoiceService, UserService userService, CinemaService cinemaService, MovieService movieService, InvoiceRepository invoiceRepository, PromotionLineService promotionLineService, ReturnInvoiceRepository returnInvoiceRepository, PromotionLineRepository promotionLineRepository, FoodService foodService, TicketService ticketService, TypeSeatService typeSeatService) {
        this.invoiceService = invoiceService;
        this.userService = userService;
        this.cinemaService = cinemaService;
        this.movieService = movieService;
        this.invoiceRepository = invoiceRepository;
        this.promotionLineService = promotionLineService;
        this.returnInvoiceRepository = returnInvoiceRepository;
        this.promotionLineRepository = promotionLineRepository;
        this.foodService = foodService;
        this.ticketService = ticketService;
        this.typeSeatService = typeSeatService;
    }

    //TODO: Thống kê doanh thu theo rạp
    @Override
    public List<ResponseRevenueByCinema> getRevenueByCinema(Integer page, Integer size, String cinemaCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
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
        List<ResponseRevenueByCinema> responseRevenueByCinemas = pageInvoices.stream().collect(Collectors.groupingBy(invoice ->
                        invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getId()))
                .entrySet().stream()
                .map(entry -> {
                    ResponseRevenueByCinema responseRevenueByCinema = new ResponseRevenueByCinema();
                    Cinema cinema = cinemaService.findById(entry.getKey()); // Lấy thông tin rạp từ cơ sở dữ liệu
                    responseRevenueByCinema.setCode(cinema.getCode());
                    responseRevenueByCinema.setName(cinema.getName());
                    Address address = cinema.getAddress();
                    String addressString = address.getStreet() + ", " + address.getWard() + ", " + address.getDistrict() + ", " + address.getCity();
                    responseRevenueByCinema.setAddress(addressString);
                    responseRevenueByCinema.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của rạp
                    responseRevenueByCinema.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của rạp
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của rạp
                    responseRevenueByCinema.setTotalRevenue(totalRevenue);
                    return responseRevenueByCinema;
                }).collect(Collectors.toList());
        // Sắp xếp kết quả nếu sort type là totalRevenue thì sort theo tổng tiền còn nếu là name thì sort theo tên
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByCinemas.sort(Comparator.comparing(ResponseRevenueByCinema::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByCinemas.sort(Comparator.comparing(ResponseRevenueByCinema::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByCinemas.sort(Comparator.comparing(ResponseRevenueByCinema::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByCinemas.sort(Comparator.comparing(ResponseRevenueByCinema::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, responseRevenueByCinemas.size());
        return responseRevenueByCinemas.subList(fromIndex, toIndex);
    }

    //TODO: Thống kê doanh thu theo phim
    @Override
    public List<ResponseRevenueByMovie> getRevenueByMovie(Integer page, Integer size, String movieCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
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
        List<ResponseRevenueByMovie> responseRevenueByMovies = pageInvoices.stream().collect(Collectors.groupingBy(invoice ->
                        invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getId()))
                .entrySet().stream()
                .map(entry -> {
                    ResponseRevenueByMovie responseRevenueByMovie = new ResponseRevenueByMovie();
                    Movie movie = movieService.findById(entry.getKey()); // Lấy thông tin phim từ cơ sở dữ liệu
                    responseRevenueByMovie.setCode(movie.getCode());
                    responseRevenueByMovie.setName(movie.getName());
                    responseRevenueByMovie.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của phim
                    responseRevenueByMovie.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của phim
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của phim
                    responseRevenueByMovie.setTotalRevenue(totalRevenue);
                    return responseRevenueByMovie;
                }).collect(Collectors.toList());

        // Sắp xếp kết quả
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByMovies.sort(Comparator.comparing(ResponseRevenueByMovie::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByMovies.sort(Comparator.comparing(ResponseRevenueByMovie::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByMovies.sort(Comparator.comparing(ResponseRevenueByMovie::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByMovies.sort(Comparator.comparing(ResponseRevenueByMovie::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, responseRevenueByMovies.size());
        return responseRevenueByMovies.subList(fromIndex, toIndex);

    }

    @Override
    public List<ResponseRevenueByUser> getRevenueByUser(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {

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
        List<ResponseRevenueByUser> responseRevenueByUsers = pageInvoices.stream().collect(Collectors.groupingBy(invoice -> invoice.getUser().getId()))
                .entrySet().stream()
                .map(entry -> {
                    ResponseRevenueByUser responseRevenueByUser = new ResponseRevenueByUser();
                    User user = userService.findById(entry.getKey()); // Lấy thông tin người dùng từ cơ sở dữ liệu
                    responseRevenueByUser.setCode(user.getCode());
                    responseRevenueByUser.setName(user.getUsername());
                    responseRevenueByUser.setEmail(user.getEmail());
                    responseRevenueByUser.setPhone(user.getPhone());
                    responseRevenueByUser.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của người dùng
                    responseRevenueByUser.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của người dùng
                    //tính chiếu khấu của hóa đơn mà khác hàng nhận được
                    //lấy danh sách hóa đơn của user
                    List<Invoice> invoices = invoiceService.findByUserId(user.getId());
                    BigDecimal totalDiscount = BigDecimal.ZERO;
                    for (Invoice invoice : invoices) {
                        //lấy khuyến mãi có loại là discount
                        PromotionLine promotionLine = invoice.getPromotionLines().stream().filter(promotionLine1 -> promotionLine1.getTypePromotion().equals(ETypePromotion.DISCOUNT)).findFirst().orElse(null);
                        if (promotionLine != null) {
                            if (promotionLine.getPromotionDiscountDetail().getTypeDiscount().equals(ETypeDiscount.PERCENT)) {
                                // tính số giảm giá
                                BigDecimal discountPercent = invoice.getTotalPrice().multiply(promotionLine.getPromotionDiscountDetail().getDiscountValue().divide(BigDecimal.valueOf(100)));
                                if (discountPercent.compareTo(BigDecimal.valueOf(promotionLine.getPromotionDiscountDetail().getMaxValue())) == 1) {
                                    totalDiscount = totalDiscount.add(BigDecimal.valueOf(promotionLine.getPromotionDiscountDetail().getMaxValue()));
                                } else {
                                    totalDiscount = totalDiscount.add(discountPercent);
                                }
                            } else {
                                totalDiscount = totalDiscount.add(promotionLine.getPromotionDiscountDetail().getDiscountValue());
                            }
                        } else {
                            totalDiscount = totalDiscount.add(BigDecimal.ZERO);
                        }

                    }
                    responseRevenueByUser.setTotalDiscount(totalDiscount);
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của người dùng
                    responseRevenueByUser.setTotalRevenue(totalRevenue);
                    return responseRevenueByUser;
                }).collect(Collectors.toList());


        // Sắp xếp kết quả
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, responseRevenueByUsers.size());
        return responseRevenueByUsers.subList(fromIndex, toIndex);


    }

    @Override
    public List<ResponseRevenueByUser> getRevenueByStaff(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
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
        List<ResponseRevenueByUser> responseRevenueByUsers = pageInvoices.stream().filter(invoice -> invoice.getStaff() != null)
                .collect(Collectors.groupingBy(invoice -> invoice.getStaff().getId()))
                .entrySet().stream()
                .map(entry -> {
                    ResponseRevenueByUser responseRevenueByUser = new ResponseRevenueByUser();
                    User user = userService.findById(entry.getKey()); // Lấy thông tin nhân viên từ cơ sở dữ liệu
                    responseRevenueByUser.setCode(user.getCode());
                    responseRevenueByUser.setName(user.getUsername());
                    responseRevenueByUser.setEmail(user.getEmail());
                    responseRevenueByUser.setPhone(user.getPhone());
                    responseRevenueByUser.setTotalInvoice(entry.getValue().size()); // Số lượng hóa đơn là số lượng hóa đơn của nhân viên
                    responseRevenueByUser.setTotalTicket(entry.getValue().stream().mapToInt(invoice -> invoice.getInvoiceTicketDetails().size()).sum()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của nhân viên
//                    BigDecimal totalDiscount = entry.getValue().stream().map(Invoice::getPromotionLines).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng giảm giá từ tổng giá trị của các hóa đơn của nhân viên
//                    responseRevenueByUser.setTotalDiscount(totalDiscount);
                    BigDecimal totalRevenue = entry.getValue().stream().map(Invoice::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add); // Tính tổng doanh thu từ tổng giá trị của các hóa đơn của nhân viên
                    responseRevenueByUser.setTotalRevenue(totalRevenue);
                    return responseRevenueByUser;
                }).collect(Collectors.toList());
        // Sắp xếp kết quả
        if (sortType.equalsIgnoreCase("total")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getTotalRevenue));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getTotalRevenue).reversed());
            }
        } else if (sortType.equalsIgnoreCase("name")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getName));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenueByUsers.sort(Comparator.comparing(ResponseRevenueByUser::getName).reversed());
            }
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, responseRevenueByUsers.size());
        return responseRevenueByUsers.subList(fromIndex, toIndex);

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

    @Override
    public List<ResponseRevenuePromotionLine> getRevenueByPromotionLine(Integer page, Integer size, String promotionLineCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection) {
        List<PromotionLine> pagePromotionLines = promotionLineRepository.findAll();
        // Lựa chọn phương thức thống kê dựa trên các điều kiện đầu vào
        if (!promotionLineCode.isEmpty() && !promotionLineCode.isBlank() && startDate != null && endDate != null) {
            pagePromotionLines = pagePromotionLines.stream().filter(promotionLine -> promotionLine.getCode().equals(promotionLineCode)
                    && promotionLine.getStartDate().isAfter(startDate.atStartOfDay()) && promotionLine.getStartDate().isBefore(endDate.atStartOfDay().plusDays(1))
            ).collect(Collectors.toList());
        } else if (startDate != null && endDate != null) {
            pagePromotionLines = pagePromotionLines.stream().filter(promotionLine -> promotionLine.getStartDate().isAfter(startDate.atStartOfDay())
                    && promotionLine.getStartDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        // Tính toán tổng số lượng hóa đơn, tổng số lượng vé và tổng doanh thu
        List<ResponseRevenuePromotionLine> responseRevenuePromotionLines = pagePromotionLines.stream().map(promotionLine -> {
            ResponseRevenuePromotionLine responseRevenuePromotionLine = new ResponseRevenuePromotionLine();
            responseRevenuePromotionLine.setCode(promotionLine.getCode());
            responseRevenuePromotionLine.setName(promotionLine.getName());
            responseRevenuePromotionLine.setImage(promotionLine.getImage());
            responseRevenuePromotionLine.setStartDate(promotionLine.getStartDate());
            responseRevenuePromotionLine.setEndDate(promotionLine.getEndDate());
            responseRevenuePromotionLine.setPromotionType(String.valueOf(promotionLine.getTypePromotion()));
            if (!promotionLine.getTypePromotion().equals(ETypePromotion.DISCOUNT)) {
                if (promotionLine.getTypePromotion().equals(ETypePromotion.FOOD)) {
                    Food food = foodService.findById(promotionLine.getPromotionFoodDetail().getFoodPromotion());
                    responseRevenuePromotionLine.setPromotionCode(food.getCode());
                    responseRevenuePromotionLine.setPromotionName(food.getName());
                    responseRevenuePromotionLine.setPromotionQuantity(promotionLine.getPromotionFoodDetail().getQuantityPromotion());
                }
                if (promotionLine.getTypePromotion().equals(ETypePromotion.TICKET)) {
                    TypeSeat typeSeat = typeSeatService.findById(promotionLine.getPromotionTicketDetail().getTypeSeatPromotion());
                    responseRevenuePromotionLine.setPromotionCode(typeSeat.getCode());
                    responseRevenuePromotionLine.setPromotionName(String.valueOf(typeSeat.getName()));
                    responseRevenuePromotionLine.setPromotionQuantity(promotionLine.getPromotionTicketDetail().getQuantityPromotion());
                }
            } else {
                responseRevenuePromotionLine.setPromotionValue(promotionLine.getPromotionDiscountDetail().getDiscountValue());
                responseRevenuePromotionLine.setValueType(String.valueOf(promotionLine.getPromotionDiscountDetail().getTypeDiscount()));
            }
            responseRevenuePromotionLine.setQuantityNotUsed(promotionLine.getQuantity()); // Tổng số vé là tổng số vé của tất cả các hóa đơn của khuyến mãi
            //lấy số lượng hóa đơn đã sử dụng promotion
            List<Invoice> invoices = invoiceService.getInvoiceByPromotionLineId(promotionLine.getId());
            int quantityUsed = invoices.size();
            responseRevenuePromotionLine.setQuantityUsed(quantityUsed);
            responseRevenuePromotionLine.setTotalQuantity(promotionLine.getQuantity() + quantityUsed);
            return responseRevenuePromotionLine;
        }).collect(Collectors.toList());

        // Sắp xếp kết quả

        if (sortType.equalsIgnoreCase("date")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenuePromotionLines.sort(Comparator.comparing(ResponseRevenuePromotionLine::getStartDate));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenuePromotionLines.sort(Comparator.comparing(ResponseRevenuePromotionLine::getStartDate).reversed());
            }

        } else if (sortType.equalsIgnoreCase("quantityUsed")) {
            if (sortDirection == Sort.Direction.ASC) {
                responseRevenuePromotionLines.sort(Comparator.comparing(ResponseRevenuePromotionLine::getQuantityUsed));
            } else if (sortDirection == Sort.Direction.DESC) {
                responseRevenuePromotionLines.sort(Comparator.comparing(ResponseRevenuePromotionLine::getQuantityUsed).reversed());
            }


        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, responseRevenuePromotionLines.size());
        return responseRevenuePromotionLines.subList(fromIndex, toIndex);

    }

}
