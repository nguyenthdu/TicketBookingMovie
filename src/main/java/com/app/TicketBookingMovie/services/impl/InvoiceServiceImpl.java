package com.app.TicketBookingMovie.services.impl;


import com.app.TicketBookingMovie.dtos.*;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.app.TicketBookingMovie.repository.InvoiceRepository;
import com.app.TicketBookingMovie.repository.PriceDetailRepository;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.services.FoodService;
import com.app.TicketBookingMovie.services.InvoiceService;
import com.app.TicketBookingMovie.services.TicketService;
import com.app.TicketBookingMovie.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TicketService ticketService;
    private final FoodService foodService;
    private final UserService userService;
    private final PriceDetailRepository priceDetailRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final ModelMapper modelMapper;


    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, TicketService ticketService, FoodService foodService, UserService userService, PriceDetailRepository priceDetailRepository, PromotionLineRepository promotionLineRepository, ModelMapper modelMapper) {
        this.invoiceRepository = invoiceRepository;
        this.ticketService = ticketService;
        this.foodService = foodService;
        this.userService = userService;
        this.priceDetailRepository = priceDetailRepository;
        this.promotionLineRepository = promotionLineRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId, Set<Long> promotionLineIds) {
        // Tạo một đối tượng Invoice mới
        Invoice invoice = new Invoice();
        invoice.setCode(randomCode()); // Tạo mã hóa đơn
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(true);
        // Bước 3: Tìm chiến lược khuyến mãi
        LocalDateTime currentTime = LocalDateTime.now();
        List<PriceDetail> currentPriceDetails = priceDetailRepository.findCurrentSalePriceDetails(currentTime);
        // Tạo danh sách chi tiết vé từ thông tin vé
        List<InvoiceTicketDetail> invoiceTicketDetails = new ArrayList<>();
        List<Ticket> tickets = ticketService.createTickets(showTimeId, seatIds);
        for (Ticket ticket : tickets) {
            InvoiceTicketDetail ticketDetail = new InvoiceTicketDetail();
            ticketDetail.setTicket(ticket);
            ticketDetail.setQuantity(1); // Mỗi vé là 1 sản phẩm
//            Optional<PriceDetail> saleDetail = currentPriceDetails.stream()
//                    .filter(s -> s.getTypeSeat().getId().equals(ticket.getSeat().getSeatType().getId()))
//                    .findFirst();
//            if (saleDetail.isPresent() && saleDetail.get().isStatus()) {
//                ticketDetail.setPrice(saleDetail.get().getPrice() + ticket.getShowTime().getRoom().getPrice());
//                if (saleDetail.get().getPrice() > ticket.getSeat().getSeatType().getPrice()) {
//                    ticketDetail.setNote("Tăng giá");
//                } else {
//                    ticketDetail.setNote("Giảm giá");
//                }
//            } else {
//                ticketDetail.setPrice(ticket.getSeat().getSeatType().getPrice() + ticket.getShowTime().getRoom().getPrice());
//            }
            ticketDetail.setPrice(ticket.getSeat().getSeatType().getPrice().getPrice() + ticket.getShowTime().getRoom().getPrice());
            invoiceTicketDetails.add(ticketDetail);
        }
        invoice.setInvoiceTicketDetails(invoiceTicketDetails);

        // Tạo danh sách chi tiết đồ ăn từ thông tin đồ ăn
        List<InvoiceFoodDetail> invoiceFoodDetails = new ArrayList<>();
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

        // Gán người dùng và nhân viên thanh toán vào hóa đơn
        User user = userService.getCurrentUser(emailUser);
        invoice.setUser(user);
        User staff = userService.findById(staffId);
        invoice.setStaff(staff);

        // Tính tổng giá của hóa đơn
        double total = 0;
        for (InvoiceTicketDetail ticketDetail : invoiceTicketDetails) {
            total += ticketDetail.getPrice() * ticketDetail.getQuantity();
        }
        for (InvoiceFoodDetail foodDetail : invoiceFoodDetails) {
            total += foodDetail.getPrice() * foodDetail.getQuantity();
        }
        // Áp dụng khuyến mãi và cập nhật tổng giá của hóa đơn
        total = applyPromotions(promotionLineIds, invoice, total);
        invoice.setTotalPrice(total);

        // Lưu hóa đơn vào cơ sở dữ liệu
        invoiceRepository.save(invoice);
    }

    private static InvoiceFoodDetail getInvoiceFoodDetail(Food food, int quantity) {
        InvoiceFoodDetail foodDetail = new InvoiceFoodDetail();
        foodDetail.setFood(food);
        foodDetail.setQuantity(quantity);
        foodDetail.setPrice(food.getPrice().getPrice() * quantity);
        return foodDetail;
    }


    private double applyPromotions(Set<Long> promotionLineIds, Invoice invoice, double total) {
        List<PromotionLine> promotionLines = promotionLineRepository.findAllById(promotionLineIds);

        for (PromotionLine promotionLine : promotionLines) {
            PromotionDetail promotionDetail = promotionLine.getPromotionDetail();
            if (promotionDetail == null) {
                throw new AppException("Promotion detail not found for promotion line: " + promotionLine.getId(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (promotionLine.getTypePromotion() == ETypePromotion.GIFT) {
                // Áp dụng khuyến mãi loại GIFT
                applyGiftPromotion(promotionLine, promotionDetail, invoice);
            } else if (promotionLine.getTypePromotion() == ETypePromotion.DISCOUNT) {
                // Áp dụng khuyến mãi loại DISCOUNT và cập nhật tổng giá trị hóa đơn
                total = applyDiscountPromotion(promotionLine, promotionDetail, total);
            }
        }
        invoice.setPromotion(promotionLines.get(0).getPromotion());
        // Trả về tổng giá trị hóa đơn sau khi áp dụng các khuyến mãi
        return total;
    }


    private void applyGiftPromotion(PromotionLine promotionLine, PromotionDetail promotionDetail, Invoice invoice) {
        //tì đồ ăn trong khuyến mãi và  trừ đi số lượng của đồ ăn đó
        Food food = promotionDetail.getFood();
        int quantity = promotionDetail.getMaxValue();
        if (food.getQuantity() < quantity) {
            throw new AppException("Not enough stock for food: " + food.getName(), HttpStatus.BAD_REQUEST);
        }
        InvoiceFoodDetail foodDetail = new InvoiceFoodDetail();
        foodDetail.setFood(food);
        foodDetail.setQuantity(quantity);
        foodDetail.setPrice(0);
        foodDetail.setNote("Quà tặng");
        invoice.getInvoiceFoodDetails().add(foodDetail);
        food.setQuantity(food.getQuantity() - quantity);
        // Giảm số lượng khuyến mãi còn lại của promotion line
        promotionLine.setUsePerPromotion(promotionLine.getUsePerPromotion() - quantity);
    }


    private double applyDiscountPromotion(PromotionLine promotionLine, PromotionDetail promotionDetail, double total) {
        double discountValue = promotionDetail.getDiscountValue();
        if (promotionDetail.getTypeDiscount() == ETypeDiscount.PERCENT) {
            discountValue = total * discountValue / 100;
        }
        promotionLine.setUsePerPromotion(promotionLine.getUsePerPromotion() - 1);
        // Trả về giá trị mới của tổng giá trị hóa đơn sau khi áp dụng giảm giá
        return total - discountValue;
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Invoice not found", HttpStatus.NOT_FOUND));
        return modelMapper.map(invoice, InvoiceDto.class);

    }

    @Override
    public List<InvoiceDto> getAllInvoices(Integer page, Integer size, String invoiceCode, Long cinemaId, Long roomId, Long movieId, String showTimeCode, Long staffId, Long userId, String status, LocalDate dateCreated) {
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
    public long countAllInvoices(String invoiceCode, Long cinemaId, Long roomId, Long movieId, String showTimeCode, Long staffId, Long userId, String status, LocalDate dateCreated) {
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
        roomDto.setPrice(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getPrice());
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
            invoiceFoodDetailDto.setPriceItem(invoiceFoodDetail.getFood().getPrice().getPrice());
            invoiceFoodDetailDto.setPrice(invoiceFoodDetail.getPrice());
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
            invoiceTicketDetailDto.setPrice(invoiceTicketDetail.getPrice());
            invoiceTicketDetailDto.setPriceItem(invoiceTicketDetail.getTicket().getSeat().getSeatType().getPrice().getPrice());
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
