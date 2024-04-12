package com.app.TicketBookingMovie.services.impl;


import com.app.TicketBookingMovie.dtos.*;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.models.enums.EDetailType;
import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.app.TicketBookingMovie.repository.InvoiceRepository;
import com.app.TicketBookingMovie.services.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TicketService ticketService;
    private final FoodService foodService;
    private final UserService userService;
    private final PriceDetailService priceDetailService;
    private final PromotionLineService promotionLineService;
    private final ModelMapper modelMapper;


    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, TicketService ticketService, FoodService foodService, UserService userService, PriceDetailService priceDetailService, PromotionLineService promotionLineService, ModelMapper modelMapper) {
        this.invoiceRepository = invoiceRepository;
        this.ticketService = ticketService;
        this.foodService = foodService;
        this.userService = userService;
        this.priceDetailService = priceDetailService;
        this.promotionLineService = promotionLineService;
        this.modelMapper = modelMapper;
    }


    @Override
    @Transactional
    public void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId) {
        // Tạo một đối tượng Invoice mới
        Invoice invoice = new Invoice();
        invoice.setCode(randomCode()); // Tạo mã hóa đơn
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(true);
        // Bước 3: Tìm chiến lược khuyến mãi
        LocalDateTime currentTime = LocalDateTime.now();
        List<PriceDetail> currentPriceDetails = priceDetailService.priceActive();
        // Tạo danh sách chi tiết vé từ thông tin vé
        List<InvoiceTicketDetail> invoiceTicketDetails = new ArrayList<>();
        List<Ticket> tickets = ticketService.createTickets(showTimeId, seatIds);
        for (Ticket ticket : tickets) {
            InvoiceTicketDetail ticketDetail = new InvoiceTicketDetail();
            ticketDetail.setTicket(ticket);
            ticketDetail.setQuantity(1); // Mỗi vé là 1 sản phẩm
            Optional<PriceDetail> seatPriceDetailOptional = currentPriceDetails.stream()
                    .filter(detail -> detail.getType() == EDetailType.TYPE_SEAT && Objects.equals(detail.getTypeSeat().getId(), ticket.getSeat().getSeatType().getId()))
                    .findFirst();
            // Lấy giá của phòng từ PriceDetail
            Optional<PriceDetail> roomPriceDetailOptional = currentPriceDetails.stream()
                    .filter(detail -> detail.getType() == EDetailType.ROOM && Objects.equals(detail.getRoom().getId(), ticket.getShowTime().getRoom().getId()))
                    .findFirst();
            if (seatPriceDetailOptional.isPresent() && roomPriceDetailOptional.isPresent()) {
                PriceDetail seatPriceDetail = seatPriceDetailOptional.get();
                PriceDetail roomPriceDetail = roomPriceDetailOptional.get();
                //nếu trạng thái của pricedetail là false hoặc thời gian hiện tại không năm trong khoản thời gian của price header của pricedetail thì không thể tạo hóa đơn
                if (!seatPriceDetail.isStatus() || !roomPriceDetail.isStatus() || currentTime.isBefore(seatPriceDetail.getPriceHeader().getStartDate()) || currentTime.isAfter(seatPriceDetail.getPriceHeader().getEndDate()) || currentTime.isBefore(roomPriceDetail.getPriceHeader().getStartDate()) || currentTime.isAfter(roomPriceDetail.getPriceHeader().getEndDate())) {
                    throw new AppException("Giá sản phẩm không sẵn sàng để tại hóa đơn, vui lòng liên hệ quản trị viên", HttpStatus.BAD_REQUEST);
                }

                ticketDetail.setPrice(seatPriceDetail.getPrice().add(roomPriceDetail.getPrice()));
            } else {
                throw new AppException("Giá của " + ticket.getSeat().getSeatType().getName() + " hoặc " + ticket.getShowTime().getRoom().getName() + " không sẵn sàng, vui lòng liên hệ quản trị viên", HttpStatus.BAD_REQUEST);
            }
            invoiceTicketDetails.add(ticketDetail);
        }
        invoice.setInvoiceTicketDetails(invoiceTicketDetails);

        // Tạo danh sách chi tiết đồ ăn từ thông tin đồ ăn
        List<InvoiceFoodDetail> invoiceFoodDetails = new ArrayList<>();
        if (!foodIds.isEmpty()) {
            Map<Long, Integer> foodQuantityMap = new HashMap<>(); // Map lưu trữ số lượng của từng loại đồ ăn

            for (Long foodId : foodIds) {
                if (foodQuantityMap.containsKey(foodId)) {
                    foodQuantityMap.put(foodId, foodQuantityMap.get(foodId) + 1);
                } else {
                    foodQuantityMap.put(foodId, 1);
                }
            }

            for (Map.Entry<Long, Integer> entry : foodQuantityMap.entrySet()) {
                Long foodId = entry.getKey();
                int quantity = entry.getValue();
                Food food = foodService.findById(foodId);

                if (food.getQuantity() < quantity) {
                    throw new AppException("Not enough stock for food: " + food.getName(), HttpStatus.BAD_REQUEST);
                }
                // Tạo chi tiết hóa đơn cho từng loại đồ ăn, thông tin loại đồ ăn
                InvoiceFoodDetail foodDetail = getInvoiceFoodDetail(food, quantity);
                invoiceFoodDetails.add(foodDetail);
            }
            invoice.setInvoiceFoodDetails(invoiceFoodDetails);

            // Giảm số lượng tồn của sản phẩm
            for (Map.Entry<Long, Integer> entry : foodQuantityMap.entrySet()) {
                Long foodId = entry.getKey();
                int quantity = entry.getValue();
                Food food = foodService.findById(foodId);
                food.setQuantity(food.getQuantity() - quantity);
            }
        } else {
            // Nếu không có đồ ăn thì không cần tạo chi tiết đồ ăn
            invoice.setInvoiceFoodDetails(new ArrayList<>());
        }

        // Gán người dùng và nhân viên thanh toán vào hóa đơn
        User user = userService.getCurrentUser(emailUser);
        invoice.setUser(user);
        User staff = userService.findById(staffId);
        invoice.setStaff(staff);

        // Tính tổng giá của hóa đơn
        BigDecimal total = calculateTotalPrice(invoiceTicketDetails, invoiceFoodDetails);

        // Lấy danh sách PromotionLine đang hoạt động
        List<PromotionLine> promotionLines = promotionLineService.getPromotionLineActive();

        // Áp dụng khuyến mãi nếu có
        for (PromotionLine promotionLine : promotionLines) {
            if (isPromotionApplicable(promotionLine, total)) {
                total = applyPromotion(promotionLine, total);
                invoice.setPromotionLines(Set.of(promotionLine));
            }
        }

        // Lưu tổng giá của hóa đơn
        invoice.setTotalPrice(total);
        invoiceRepository.save(invoice);

    }

    private InvoiceFoodDetail getInvoiceFoodDetail(Food food, int quantity) {
        InvoiceFoodDetail foodDetail = new InvoiceFoodDetail();
        foodDetail.setFood(food);
        foodDetail.setQuantity(quantity);
        //Lấy giá cuả food trong chương trình quản lý giá và kiểm tra giá đó có còn hoạt động hay không
        List<PriceDetail> currentPriceDetails = priceDetailService.priceActive();
        Optional<PriceDetail> foodPriceDetailOptional = currentPriceDetails.stream()
                .filter(detail -> detail.getType() == EDetailType.FOOD && Objects.equals(detail.getFood().getId(), food.getId()))
                .findFirst();
        if (foodPriceDetailOptional.isPresent()) {
            PriceDetail foodPriceDetail = foodPriceDetailOptional.get();
            foodDetail.setPrice(foodPriceDetail.getPrice());
        } else {
            throw new AppException("Giá của " + food.getName() + " không sẵn sàng, vui lòng liên hệ quản trị viên", HttpStatus.BAD_REQUEST);
        }
        return foodDetail;
    }

    private BigDecimal calculateTotalPrice(List<InvoiceTicketDetail> ticketDetails, List<InvoiceFoodDetail> foodDetails) {
        // Tính tổng giá của vé
        BigDecimal ticketTotal = ticketDetails.stream()
                .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tính tổng giá của đồ ăn
        BigDecimal foodTotal = foodDetails.stream()
                .map(detail -> detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tổng giá của hóa đơn là tổng giá của vé và đồ ăn
        return ticketTotal.add(foodTotal);
    }

    private boolean isPromotionApplicable(PromotionLine promotionLine, BigDecimal total) {
        // Kiểm tra xem hóa đơn có đủ điều kiện để áp dụng khuyến mãi hay không
        PromotionDiscountDetail discountDetail = promotionLine.getPromotionDiscountDetail();
        if (discountDetail != null) {
            // Nếu loại khuyến mãi là tiền và tổng giá trị hóa đơn >= giá trị tối thiểu của khuyến mãi
            // Nếu loại khuyến mãi là phần trăm và tổng giá trị hóa đơn >= giá trị tối thiểu của khuyến mãi
            if (discountDetail.getTypeDiscount() == ETypeDiscount.AMOUNT && total.compareTo(discountDetail.getMinBillValue()) >= 0) {
                return true;
            } else
                return discountDetail.getTypeDiscount() == ETypeDiscount.PERCENT && total.compareTo(discountDetail.getMinBillValue()) >= 0;
        }

        return false;
    }

    private BigDecimal applyPromotion(PromotionLine promotionLine, BigDecimal total) {
        // Áp dụng khuyến mãi vào tổng giá trị hóa đơn
        PromotionDiscountDetail discountDetail = promotionLine.getPromotionDiscountDetail();
        if (discountDetail != null) {
            if (discountDetail.getTypeDiscount() == ETypeDiscount.AMOUNT) {
                // Nếu loại khuyến mãi là tiền, giảm giá trực tiếp từ tổng giá trị hóa đơn
                total = total.subtract(discountDetail.getDiscountValue());
                // Kiểm tra giá trị giảm giá không được vượt quá giá trị tối đa
            } else if (discountDetail.getTypeDiscount() == ETypeDiscount.PERCENT) {
                // Nếu loại khuyến mãi là phần trăm, giảm giá theo tỷ lệ phần trăm
                BigDecimal discountAmount = total.multiply(discountDetail.getDiscountValue().divide(BigDecimal.valueOf(100)));
                // Kiểm tra giá trị giảm giá không được vượt quá giá trị tối đa
                if (discountAmount.compareTo(BigDecimal.valueOf(discountDetail.getMaxValue())) > 0) {
                    discountAmount = BigDecimal.valueOf(discountDetail.getMaxValue());
                }
                total = total.subtract(discountAmount);
            }
        }
        return total;
    }


    @Override
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        return modelMapper.map(invoice, InvoiceDto.class);

    }

    @Override
    public List<InvoiceDto> getAllInvoices(Integer page, Integer size, String invoiceCode, Long cinemaId, Long
            roomId, Long movieId, String showTimeCode, Long staffId, Long userId, String status, LocalDate dateCreated) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Invoice> pageInvoice;
        if (invoiceCode != null && !invoiceCode.isEmpty()) {
            pageInvoice = invoiceRepository.findByCode(invoiceCode, pageable);
        } else if (cinemaId != null) {
            pageInvoice = invoiceRepository.findByCinemaId(cinemaId, pageable);
        } else if (roomId != null) {
            pageInvoice = invoiceRepository.findByRoomId(roomId, pageable);
        } else if (movieId != null) {
            pageInvoice = invoiceRepository.findByMovieId(movieId, pageable);
        } else if (showTimeCode != null && !showTimeCode.isEmpty()) {
            pageInvoice = invoiceRepository.findByShowTimeCode(showTimeCode, pageable);
        } else if (staffId != null) {
            pageInvoice = invoiceRepository.findByStaffId(staffId, pageable);
        } else if (userId != null) {
            pageInvoice = invoiceRepository.findByUserId(userId, pageable);
        } else if (status != null && !status.isEmpty()) {
            pageInvoice = invoiceRepository.findByStatus(status.equals("true"), pageable);
        } else if (dateCreated != null && !dateCreated.toString().isEmpty()) {
            //lọc theo ngày tháng năm
            pageInvoice = invoiceRepository.findByCreatedDate(dateCreated, pageable);
        } else {
            pageInvoice = invoiceRepository.findAll(pageable);
        }
        // tạo 1 danh sách chứa các đối tượng InvoiceDto
        List<InvoiceDto> invoiceDtos = new ArrayList<>();
        //lặp qua  các đối tượng Invoice và chuyển đổi chúng thành InvoiceDto
        for (Invoice invoice : pageInvoice) {
            InvoiceDto invoiceDto = modelMapper.map(invoice, InvoiceDto.class);
            //lấy code show time
            invoiceDto.setShowTimeCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode());
            //lấy tên rạp
            invoiceDto.setRoomName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getName());
            //lấy tên phòng
            invoiceDto.setCinemaName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getName());
            //lấy tên phim
            invoiceDto.setMovieName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getName());
            //lấy tên nhân viên
            invoiceDto.setStaffName(invoice.getStaff().getUsername());
            //lấy tên người dùng
            invoiceDto.setUserName(invoice.getUser().getUsername());
            //thêm vào danh sách
            invoiceDtos.add(invoiceDto);
        }
        return invoiceDtos.stream().sorted(Comparator.comparing(InvoiceDto::getCreatedDate).reversed())
                .toList();
    }

    @Override
    public long countAllInvoices(String invoiceCode, Long cinemaId, Long roomId, Long movieId, String
            showTimeCode, Long staffId, Long userId, String status, LocalDate dateCreated) {
        if (invoiceCode != null && !invoiceCode.isEmpty()) {
            return invoiceRepository.countByCode(invoiceCode);
        } else if (cinemaId != null) {
            return invoiceRepository.countByCinemaId(cinemaId);
        } else if (roomId != null) {
            return invoiceRepository.countByRoomId(roomId);
        } else if (movieId != null) {
            return invoiceRepository.countByMovieId(movieId);
        } else if (showTimeCode != null && !showTimeCode.isEmpty()) {
            return invoiceRepository.countByShowTimeCode(showTimeCode);
        } else if (staffId != null) {
            return invoiceRepository.countByStaffId(staffId);
        } else if (userId != null) {
            return invoiceRepository.countByUserId(userId);
        } else if (status != null && !status.isEmpty()) {
            return invoiceRepository.countByStatus(status.equals("true"));
        } else if (dateCreated != null && !dateCreated.toString().isEmpty()) {
            return invoiceRepository.countByCreatedDate(dateCreated);
        } else {
            return invoiceRepository.count();
        }
    }

    @Override
    public CinemaDto getCinemaByInvoiceId(Long id) {
        CinemaDto cinemaDto = new CinemaDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        cinemaDto.setId(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getId());
        cinemaDto.setName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getName());
        AddressDto addressDto = getAddressDto(invoice);
        cinemaDto.setAddress(addressDto);
        return cinemaDto;
    }

    private static AddressDto getAddressDto(Invoice invoice) {
        AddressDto addressDto = new AddressDto();
        addressDto.setId(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getAddress().getId());
        addressDto.setCity(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getAddress().getCity());
        addressDto.setDistrict(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getAddress().getDistrict());
        addressDto.setStreet(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getAddress().getStreet());
        return addressDto;
    }

    @Override
    public RoomDto getRoomByInvoiceId(Long id) {
        RoomDto roomDto = new RoomDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        roomDto.setId(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getId());
        roomDto.setName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getName());
//        roomDto.setPrice(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getPrice().getPrice());
        String typeRoom = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getType().toString();
        roomDto.setType(typeRoom);
        return roomDto;
    }

    @Override
    public MovieDto getMovieByInvoiceId(Long id) {
        MovieDto movieDto = new MovieDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        movieDto.setCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCode());
        movieDto.setName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getName());
        movieDto.setDurationMinutes(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDurationMinutes());
        movieDto.setReleaseDate(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getReleaseDate());
        movieDto.setImageLink(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getImageLink());
        movieDto.setDirector(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDirector());
        movieDto.setDirector(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDirector());
        movieDto.setProducer(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getProducer());
        movieDto.setCast(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCast());
        //convert genresdto
        List<GenreDto> genreDtos = new ArrayList<>();
        for (Genre genre : invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getGenres()) {
            GenreDto genreDto = modelMapper.map(genre, GenreDto.class);
            genreDtos.add(genreDto);
        }
        movieDto.setGenres(genreDtos);
        return movieDto;
    }

    @Override
    public ShowTimeDto getShowTimeByInvoiceId(Long id) {
        ShowTimeDto showTimeDto = new ShowTimeDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        showTimeDto.setCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode());
        showTimeDto.setShowDate(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowDate());
        showTimeDto.setShowTime(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowTime());
        return showTimeDto;
    }

    @Override
    public UserDto getUserByInvoiceId(Long id) {
        UserDto userDto = new UserDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        userDto.setCode(invoice.getUser().getCode());
        userDto.setUsername(invoice.getUser().getUsername());
        userDto.setEmail(invoice.getUser().getEmail());
        userDto.setPhone(invoice.getUser().getPhone());
        return userDto;
    }

    @Override
    public List<InvoiceFoodDetailDto> getInvoiceFoodDetailByInvoiceId(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        List<InvoiceFoodDetail> invoiceFoodDetails = invoiceRepository.findById(id).get().getInvoiceFoodDetails();
        List<InvoiceFoodDetailDto> invoiceFoodDetailDtos = new ArrayList<>();
        for (InvoiceFoodDetail invoiceFoodDetail : invoiceFoodDetails) {
            InvoiceFoodDetailDto invoiceFoodDetailDto = modelMapper.map(invoiceFoodDetail, InvoiceFoodDetailDto.class);
            invoiceFoodDetailDto.setFoodName(invoiceFoodDetail.getFood().getName());
            invoiceFoodDetailDto.setQuantity(invoiceFoodDetail.getQuantity());
//            invoiceFoodDetailDto.setPriceItem(invoiceFoodDetail.getFood().getPrice().getPrice());
//                invoiceFoodDetailDto.setPrice(invoiceFoodDetail.getPrice());
            invoiceFoodDetailDto.setNote(invoiceFoodDetail.getNote());
            invoiceFoodDetailDtos.add(invoiceFoodDetailDto);
        }
        return invoiceFoodDetailDtos;


    }

    @Override
    public List<InvoiceTicketDetailDto> getInvoiceTicketDetailByInvoiceId(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        List<InvoiceTicketDetail> invoiceTicketDetails = invoiceRepository.findById(id).get().getInvoiceTicketDetails();
        List<InvoiceTicketDetailDto> invoiceTicketDetailDtos = new ArrayList<>();
        for (InvoiceTicketDetail invoiceTicketDetail : invoiceTicketDetails) {
            InvoiceTicketDetailDto invoiceTicketDetailDto = modelMapper.map(invoiceTicketDetail, InvoiceTicketDetailDto.class);
            invoiceTicketDetailDto.setTicketCode(invoiceTicketDetail.getTicket().getCode());
            invoiceTicketDetailDto.setSeatName(invoiceTicketDetail.getTicket().getSeat().getName());
            invoiceTicketDetailDto.setRowCol(invoiceTicketDetail.getTicket().getSeat().getSeatRow() + " - " + invoiceTicketDetail.getTicket().getSeat().getSeatColumn());
            String typeSeat = String.valueOf(invoiceTicketDetail.getTicket().getSeat().getSeatType().getName());
            invoiceTicketDetailDto.setSeatType(typeSeat);
//                invoiceTicketDetailDto.setPrice(invoiceTicketDetail.getPrice());
//            invoiceTicketDetailDto.setPriceItem(invoiceTicketDetail.getTicket().getSeat().getSeatType().getPrice().getPrice());
            invoiceTicketDetailDto.setQuantity(invoiceTicketDetail.getQuantity());
            invoiceTicketDetailDto.setNote(invoiceTicketDetail.getNote());
            invoiceTicketDetailDtos.add(invoiceTicketDetailDto);
        }
        return invoiceTicketDetailDtos;
    }

    private String randomCode() {
        return "HD" + LocalDateTime.now().getNano();
    }
}
