package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.*;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.*;
import com.app.TicketBookingMovie.models.enums.EDetailType;
import com.app.TicketBookingMovie.models.enums.EPay;
import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.app.TicketBookingMovie.repository.InvoiceRepository;
import com.app.TicketBookingMovie.services.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final TicketService ticketService;
    private final FoodService foodService;
    private final UserService userService;
    private final PriceDetailService priceDetailService;
    private final PromotionLineService promotionLineService;
    private final ModelMapper modelMapper;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ObjectMapper redisObjectMapper;
    @Autowired
    EmailService emailService;
    @Autowired
    private JavaMailSender emailSender; // Inject JavaMailSender

    public String generateKey(Long key) {
        return "Invoice:" + key;
    }

    public void clear() {
        // xóa dữ liệu của user
        Set<Object> keys = redisTemplate.keys("Invoice:*");
        if (keys != null) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    @Transactional
    public void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId,
            String typePay) {
        // kiểm tra nếu trong 1 ngày mà user đã đặt 8 ghế trong tất cả hóa đơn của ngày
        // đó thì không được đặt nữa
        List<Invoice> invoices = invoiceRepository.findInvoiceByToday(LocalDateTime.now().toLocalDate());
        // long count = invoices.stream().filter(invoice ->
        // invoice.getUser().getEmail().equals(emailUser)).count();
        // if (count >= 8) {
        // throw new AppException("Không thể đặt quá 8 ghế trong 1 ngày",
        // HttpStatus.BAD_REQUEST);
        // }
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
                    .filter(detail -> detail.getType() == EDetailType.TYPE_SEAT
                            && Objects.equals(detail.getTypeSeat().getId(), ticket.getSeat().getSeatType().getId()))
                    .findFirst();
            // Lấy giá của phòng từ PriceDetail
            Optional<PriceDetail> roomPriceDetailOptional = currentPriceDetails.stream()
                    .filter(detail -> detail.getType() == EDetailType.ROOM
                            && Objects.equals(detail.getRoom().getId(), ticket.getShowTime().getRoom().getId()))
                    .findFirst();
            if (seatPriceDetailOptional.isPresent() && roomPriceDetailOptional.isPresent()) {
                PriceDetail seatPriceDetail = seatPriceDetailOptional.get();
                PriceDetail roomPriceDetail = roomPriceDetailOptional.get();
                // nếu trạng thái của pricedetail là false thì không thể tạo hóa đơn
                if (!seatPriceDetail.isStatus()) {
                    throw new AppException("Giá của " + ticket.getSeat().getSeatType().getName()
                            + " không sẵn sàng, vui lòng liên hệ quản trị viên", HttpStatus.BAD_REQUEST);
                }
                if (!roomPriceDetail.isStatus()) {
                    throw new AppException("Giá của " + ticket.getShowTime().getRoom().getName()
                            + " không sẵn sàng, vui lòng liên hệ quản trị viên", HttpStatus.BAD_REQUEST);
                }
                ticketDetail.setPrice(seatPriceDetail.getPrice().add(roomPriceDetail.getPrice()));
            } else {
                throw new AppException("Giá của " + ticket.getSeat().getSeatType().getName() + " hoặc "
                        + ticket.getShowTime().getRoom().getName() + " không sẵn sàng, vui lòng liên hệ quản trị viên",
                        HttpStatus.BAD_REQUEST);
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
                    throw new AppException("Số lượng đồ ăn: " + food.getName() + " không đủ!!!",
                            HttpStatus.BAD_REQUEST);
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
        if (staffId != null) {
            User staff = userService.findById(staffId);
            invoice.setStaff(staff);
        } else {
            invoice.setStaff(null);
        }

        // Tính tổng giá của hóa đơn

        // Lấy danh sách PromotionLine đang hoạt động
        List<PromotionLine> promotionLines = promotionLineService.getPromotionLineActive();
        boolean checkPromotionFood = applyAllFoodPromotions(promotionLines, invoiceFoodDetails);
        boolean checkPromotionTicket = applyAllTicketPromotions(promotionLines, invoiceTicketDetails);
        BigDecimal total = calculateTotalPrice(invoiceTicketDetails, invoiceFoodDetails);
        PromotionLine promotionLineFood;
        PromotionLine promotionLineTicket;
        ArrayList<PromotionLine> promotionApply = new ArrayList<>();
        // Áp dụng khuyến mãi nếu có
        for (PromotionLine promotionLine : promotionLines) {
            if (isPromotionApplicable(promotionLine, total)) {
                total = applyPromotion(promotionLine, total);
                promotionApply.add(promotionLine);
            }
        }
        if (checkPromotionFood) {
            promotionLineFood = promotionLines.stream()
                    .filter(promotionLine -> promotionLine.getPromotionFoodDetail() != null).findFirst().get();
            promotionApply.add(promotionLineFood);
        }
        if (checkPromotionTicket) {
            promotionLineTicket = promotionLines.stream()
                    .filter(promotionLine -> promotionLine.getPromotionTicketDetail() != null).findFirst().get();
            promotionApply.add(promotionLineTicket);
        }
        invoice.setPromotionLines(promotionApply);

        // loại thanh toán
        switch (typePay) {
            case "CASH":
                invoice.setTypePay(EPay.CASH);
                break;
            case "VNPAY":
                invoice.setTypePay(EPay.VNPAY);
                break;
            default:
                throw new AppException("Loại thanh toán không hợp lệ", HttpStatus.BAD_REQUEST);
        }
        // Lưu tổng giá của hóa đơn
        invoice.setTotalPrice(total);
        invoiceRepository.save(invoice);
        // SimpleMailMessage mailMessage = new SimpleMailMessage();
        // mailMessage.setTo(user.getEmail());
        // mailMessage.setSubject("Đặt vé thành công!");
        // mailMessage.setText("Thông tin đơn hàng");
        // emailService.sendEmail(mailMessage);
        // Construct the HTML content for the email
        String htmlContent = constructEmailContent(invoice);

        // Send the email
        // nếu email không bắt đầu bằng guest thì gửi email
        if (!emailUser.startsWith("guest")) {
            sendEmail(emailUser, "Đặt vé thành công", htmlContent);
        }
        clear();

    }

    private void sendEmail(String recipient, String subject, String content) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(content, true);
            emailSender.send(message);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }

    private InvoiceFoodDetail getInvoiceFoodDetail(Food food, int quantity) {
        InvoiceFoodDetail foodDetail = new InvoiceFoodDetail();
        foodDetail.setFood(food);
        foodDetail.setQuantity(quantity);
        // Lấy giá cuả food trong chương trình quản lý giá và kiểm tra giá đó có còn
        // hoạt động hay không
        List<PriceDetail> currentPriceDetails = priceDetailService.priceActive();
        Optional<PriceDetail> foodPriceDetailOptional = currentPriceDetails.stream()
                .filter(detail -> detail.getType() == EDetailType.FOOD
                        && Objects.equals(detail.getFood().getId(), food.getId()))
                .findFirst();
        // nếu trạng thái giá là false thì không thể tạo hóa đơn
        if (foodPriceDetailOptional.isPresent()) {
            PriceDetail foodPriceDetail = foodPriceDetailOptional.get();
            if (!foodPriceDetail.isStatus()) {
                throw new AppException("Giá của " + food.getName() + " không sẵn sàng, vui lòng liên hệ quản trị viên",
                        HttpStatus.BAD_REQUEST);
            }
            foodDetail.setPrice(foodPriceDetail.getPrice());
        } else {
            throw new AppException("Giá của " + food.getName() + " không sẵn sàng, vui lòng liên hệ quản trị viên",
                    HttpStatus.BAD_REQUEST);
        }
        return foodDetail;
    }

    // TODO: tính tổng giá của hóa đơn
    private BigDecimal calculateTotalPrice(List<InvoiceTicketDetail> ticketDetails,
            List<InvoiceFoodDetail> foodDetails) {
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
    // TODO: kiểm tra xem hóa đơn có đủ điều kiện để áp dụng khuyến mãi hay không

    private boolean isPromotionApplicable(PromotionLine promotionLine, BigDecimal total) {
        // Kiểm tra xem hóa đơn có đủ điều kiện để áp dụng khuyến mãi hay không
        PromotionDiscountDetail discountDetail = promotionLine.getPromotionDiscountDetail();
        if (discountDetail != null) {
            // Nếu loại khuyến mãi là tiền và tổng giá trị hóa đơn >= giá trị tối thiểu của
            // khuyến mãi
            // Nếu loại khuyến mãi là phần trăm và tổng giá trị hóa đơn >= giá trị tối thiểu
            // của khuyến mãi
            if (discountDetail.getTypeDiscount() == ETypeDiscount.AMOUNT
                    && total.compareTo(discountDetail.getMinBillValue()) >= 0) {
                return true;
            } else
                return discountDetail.getTypeDiscount() == ETypeDiscount.PERCENT
                        && total.compareTo(discountDetail.getMinBillValue()) >= 0;
        }

        return false;
    }

    // TODO: áp dụng khuyến mãi vào tổng giá trị hóa đơn
    private BigDecimal applyPromotion(PromotionLine promotionLine, BigDecimal total) {
        // Áp dụng khuyến mãi vào tổng giá trị hóa đơn
        PromotionDiscountDetail discountDetail = promotionLine.getPromotionDiscountDetail();
        if (discountDetail != null) {
            if (discountDetail.getTypeDiscount() == ETypeDiscount.AMOUNT) {
                // Nếu loại khuyến mãi là tiền, giảm giá trực tiếp từ tổng giá trị hóa đơn
                total = total.subtract(discountDetail.getDiscountValue());
                promotionLineService.updateQuantityPromotionLine(promotionLine.getId(), -1);
                // Kiểm tra giá trị giảm giá không được vượt quá giá trị tối đa
            } else if (discountDetail.getTypeDiscount() == ETypeDiscount.PERCENT) {
                // Nếu loại khuyến mãi là phần trăm, giảm giá theo tỷ lệ phần trăm
                BigDecimal discountAmount = total
                        .multiply(discountDetail.getDiscountValue().divide(BigDecimal.valueOf(100)));
                // Kiểm tra giá trị giảm giá không được vượt quá giá trị tối đa
                if (discountAmount.compareTo(BigDecimal.valueOf(discountDetail.getMaxValue())) > 0) {
                    discountAmount = BigDecimal.valueOf(discountDetail.getMaxValue());
                }
                total = total.subtract(discountAmount);
                promotionLineService.updateQuantityPromotionLine(promotionLine.getId(), -1);
            }
        }
        return total;
    }

    private boolean applyFoodPromotion(PromotionLine promotionLine, List<InvoiceFoodDetail> invoiceFoodDetails) {
        PromotionFoodDetail promotionFoodDetail = promotionLine.getPromotionFoodDetail();
        if (invoiceFoodDetails.isEmpty()) {
            return false;
        }
        Long foodRequiredId = promotionFoodDetail.getFoodRequired();
        int quantityRequired = promotionFoodDetail.getQuantityRequired();
        Long foodPromotionId = promotionFoodDetail.getFoodPromotion();
        int quantityPromotion = promotionFoodDetail.getQuantityPromotion();
        BigDecimal promotionPrice = promotionFoodDetail.getPrice();

        // Kiểm tra xem trong danh sách chi tiết đồ ăn có đủ điều kiện để áp dụng khuyến
        // mãi hay không
        InvoiceFoodDetail requiredFoodDetail = invoiceFoodDetails.stream()
                .filter(detail -> detail.getFood().getId().equals(foodRequiredId))
                .findFirst()
                .orElse(null);
        // nếu đô ăn truyền vào không có trong hóa đơn thì trả về false
        if (requiredFoodDetail == null) {
            return false;
        }
        // lấy hóa đơn có khuyến mãi
        InvoiceFoodDetail promotionFood = invoiceFoodDetails.stream()
                .filter(detail -> detail.getFood().getId().equals(foodPromotionId))
                .findFirst()
                .orElse(null);
        if (promotionFood == null) {
            return false;
        }
        if (promotionFood.getFood().getQuantity() < quantityPromotion) {
            throw new AppException("Số lượng đồ ăn khuyến mãi không đủ để áp dụng khuyến mãi!!!",
                    HttpStatus.BAD_REQUEST);
        }
        if (Objects.equals(foodRequiredId, foodPromotionId)) {
            if (promotionFood.getQuantity() - quantityRequired < quantityPromotion) {
                quantityPromotion = promotionFood.getQuantity() - quantityRequired;
            }
        } else {
            if (promotionFood.getQuantity() < quantityPromotion) {
                quantityPromotion = promotionFood.getQuantity();
            }
        }

        if (requiredFoodDetail.getQuantity() >= quantityRequired && quantityPromotion > 0) {
            if (quantityPromotion == promotionFood.getQuantity()) {
                promotionFood.setPrice(promotionPrice);
                promotionFood.setNote("Khuyến mãi");
                promotionLineService.updateQuantityPromotionLine(promotionLine.getId(), -1);
            } else {

                // Tạo InvoiceFoodDetail mới cho đồ ăn được tặng
                InvoiceFoodDetail promotionFoodDetail1 = new InvoiceFoodDetail();
                promotionFoodDetail1.setFood(foodService.findById(foodPromotionId));
                promotionFoodDetail1.setQuantity(quantityPromotion);
                promotionFoodDetail1.setPrice(promotionPrice);
                promotionFoodDetail1.setNote("Khuyến mãi");

                // Thêm vào danh sách chi tiết hóa đơn
                invoiceFoodDetails.add(promotionFoodDetail1);

                // Giảm số lượng của đồ ăn cần mua
                promotionFood.setQuantity(promotionFood.getQuantity() - quantityPromotion);
                promotionLineService.updateQuantityPromotionLine(promotionLine.getId(), -1);

            }
            return true;

        }
        return false;
    }

    private boolean applyAllFoodPromotions(List<PromotionLine> promotionLines,
            List<InvoiceFoodDetail> invoiceFoodDetails) {
        for (PromotionLine promotionLine : promotionLines) {
            if (promotionLine.getPromotionFoodDetail() != null) {
                if (applyFoodPromotion(promotionLine, invoiceFoodDetails)) {
                    return true;
                }
            }
        }
        return false;
    }

    // TODO: khuyến mãi loại ghế
    private boolean applyTicketPromotion(PromotionLine promotionLine, List<InvoiceTicketDetail> invoiceTicketDetails) {
        if (invoiceTicketDetails.isEmpty()) {
            return false;
        }
        PromotionTicketDetail promotionTicketDetail = promotionLine.getPromotionTicketDetail();
        Long typeSeatRequiredId = promotionTicketDetail.getTypeSeatRequired();
        int quantityRequired = promotionTicketDetail.getQuantityRequired();
        Long typeSeatFreeId = promotionTicketDetail.getTypeSeatPromotion();
        int quantityFree = promotionTicketDetail.getQuantityPromotion();
        BigDecimal promotionPrice = promotionTicketDetail.getPrice();

        // Kiểm tra xem loại ghế của ghế có đủ điều kiện để áp dụng khuyến mãi hay không
        List<InvoiceTicketDetail> requiredTicketDetails = invoiceTicketDetails.stream()
                .filter(detail -> detail.getTicket().getSeat().getSeatType().getId().equals(typeSeatRequiredId))
                .toList();
        // lấy danh sách loại ghế khuyến mãi typeSeatFreeId
        List<InvoiceTicketDetail> promotionTicketDetails = invoiceTicketDetails.stream()
                .filter(detail -> detail.getTicket().getSeat().getSeatType().getId().equals(typeSeatFreeId))
                .toList();
        // đếm số lượng food được khuyến mãi có trong hoá đơn
        int countPromotionTicket = (int) invoiceTicketDetails.stream()
                .filter(detail -> detail.getTicket().getSeat().getSeatType().getId().equals(typeSeatFreeId))
                .count();
        // nếu số lượng yêu cầu lớn hơn số lượng có trong hóa đơn thì số lượng khuyến
        // mãi = số luượng đã chọn
        if (Objects.equals(typeSeatRequiredId, typeSeatFreeId)) {
            if (countPromotionTicket - quantityRequired < quantityFree) {
                quantityFree = countPromotionTicket - quantityRequired;
            }
        } else {
            if (countPromotionTicket < quantityFree) {
                quantityFree = countPromotionTicket;
            }
        }
        int requiredTicketCount = requiredTicketDetails.size();

        if (requiredTicketCount >= quantityRequired
                && (quantityRequired + quantityFree) <= invoiceTicketDetails.size()) {
            // Tính toán số lượng ghế được khuyến mãi
            // Tạo InvoiceTicketDetail mới cho ghế được khuyến mãi
            for (int i = 0; i < quantityFree; i++) {
                InvoiceTicketDetail promotionTicketDetail1 = promotionTicketDetails.get(i);
                promotionTicketDetail1.setPrice(promotionPrice);
                promotionTicketDetail1.setNote("Khuyến mãi");
                promotionLineService.updateQuantityPromotionLine(promotionLine.getId(), -1);

            }
            return true;

        }
        return false;
    }

    private boolean applyAllTicketPromotions(List<PromotionLine> promotionLines,
            List<InvoiceTicketDetail> invoiceTicketDetails) {
        for (PromotionLine promotionLine : promotionLines) {
            if (promotionLine.getPromotionTicketDetail() != null) {
                if (applyTicketPromotion(promotionLine, invoiceTicketDetails)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public InvoiceDto getInvoiceById(Long id) {

        String key = generateKey(id);
        Object cachedData = redisTemplate.opsForValue().get(key);
        if (cachedData == null) {
            Invoice invoice = invoiceRepository.findById(id)
                    .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
            InvoiceDto invoiceDto = modelMapper.map(invoice, InvoiceDto.class);
            invoiceDto.setShowTimeCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode());
            invoiceDto.setRoomName(
                    invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getName());
            invoiceDto.setCinemaName(
                    invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getName());
            invoiceDto.setMovieName(
                    invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getName());
            invoiceDto.setMovieImage(
                    invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getImageLink());
            if (invoice.getStaff() == null) {
                invoiceDto.setStaffName(null);
            } else {
                invoiceDto.setStaffName(invoice.getStaff().getUsername());
            }
            invoiceDto.setUserName(invoice.getUser().getUsername());
            redisTemplate.opsForValue().set(key, invoiceDto);
            return invoiceDto;
        } else {
            return redisObjectMapper.convertValue(cachedData, InvoiceDto.class);
        }
        //
        // Invoice invoice = invoiceRepository.findById(id)
        // .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: "+ id,
        // HttpStatus.NOT_FOUND));
        // InvoiceDto invoiceDto = modelMapper.map(invoice, InvoiceDto.class);
        // invoiceDto.setShowTimeCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode());
        // invoiceDto.setRoomName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getName());
        // invoiceDto.setCinemaName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getName());
        // invoiceDto.setMovieName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getName());
        // invoiceDto.setMovieImage(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getImageLink());
        // invoiceDto.setStaffName(invoice.getStaff().getUsername());
        // invoiceDto.setUserName(invoice.getUser().getUsername());
        // return invoiceDto;

    }

    @Override
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public List<Invoice> findByUserId(Long id) {
        return invoiceRepository.findByUserId(id);
    }

    @Override
    public void updateStatusInvoice(Long id, boolean status) {
        Invoice invoice = findById(id);
        invoice.setStatus(status);
        invoiceRepository.save(invoice);
        clear();

    }

    @Override
    public List<InvoiceDto> getAllInvoices(Integer page, Integer size, String invoiceCode, Long cinemaId, Long roomId,
            Long movieId, String showTimeCode, Long staffId, Long userId, LocalDate startDate, LocalDate endDate) {
        String key = "Invoice:all";
        Object cachedData = redisTemplate.opsForValue().get(key);
        List<Invoice> invoices = List.of();
        List<InvoiceDto> invoiceDtos;
        if (cachedData == null) {
            invoices = invoiceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
            invoiceDtos = invoices.stream()
                    .map(invoice -> {
                        InvoiceDto invoiceDto = modelMapper.map(invoice, InvoiceDto.class);
                        invoiceDto.setShowTimeCode(
                                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode());
                        invoiceDto.setRoomId(
                                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getId());
                        invoiceDto.setRoomName(
                                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getName());
                        invoiceDto.setCinemaId(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime()
                                .getRoom().getCinema().getId());
                        invoiceDto.setCinemaName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime()
                                .getRoom().getCinema().getName());
                        invoiceDto.setMovieId(
                                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getId());
                        invoiceDto.setMovieName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime()
                                .getMovie().getName());
                        invoiceDto.setMovieImage(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime()
                                .getMovie().getImageLink());
                        if (invoice.getStaff() == null) {
                            invoiceDto.setStaffName(null);
                            invoiceDto.setStaffId(invoice.getStaff().getId());
                        } else {
                            invoiceDto.setStaffName(invoice.getStaff().getUsername());
                            invoiceDto.setStaffId(invoice.getStaff().getId());
                        }
                        invoiceDto.setUserId(invoice.getUser().getId());
                        invoiceDto.setUserName(invoice.getUser().getUsername());
                        return invoiceDto;
                    }).collect(Collectors.toList());
            redisTemplate.opsForValue().set(key, invoiceDtos);
        } else {
            List<Object> list = (List<Object>) cachedData;
            invoiceDtos = list.stream().map(o -> redisObjectMapper.convertValue(o, InvoiceDto.class)).toList();
        }

        if (invoiceCode != null && !invoiceCode.isEmpty()) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getCode().equals(invoiceCode))
                    .toList();
        } else if (cinemaId != null) {
            invoiceDtos = invoiceDtos.stream().filter(invoice -> invoice.getCinemaId().equals(cinemaId)).toList();
        } else if (roomId != null) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getRoomId().equals(roomId))
                    .toList();
        } else if (movieId != null) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getMovieId().equals(movieId))
                    .toList();
        } else if (showTimeCode != null && !showTimeCode.isEmpty()) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getShowTimeCode().equals(showTimeCode))
                    .toList();
        } else if (staffId != null) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getStaffId().equals(staffId))
                    .toList();
        } else if (userId != null) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getUserId().equals(userId))
                    .toList();
        } else if (startDate != null && endDate != null) {
            invoiceDtos = invoiceDtos.stream()
                    .filter(invoice -> invoice.getCreatedDate().isAfter(startDate.atStartOfDay())
                            && invoice.getCreatedDate().isBefore(endDate.atStartOfDay()))
                    .toList();
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, invoiceDtos.size());
        return invoiceDtos.subList(fromIndex, toIndex);

    }

    @Override
    public long countAllInvoices(String invoiceCode, Long cinemaId, Long roomId, Long movieId, String showTimeCode,
            Long staffId, Long userId, LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = invoiceRepository.findAll();
        if (invoiceCode != null && !invoiceCode.isEmpty()) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getCode().equals(invoiceCode))
                    .toList();
        } else if (cinemaId != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom()
                            .getCinema().getId().equals(cinemaId))
                    .toList();
        } else if (roomId != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom()
                            .getId().equals(roomId))
                    .toList();
        } else if (movieId != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie()
                            .getId().equals(movieId))
                    .toList();
        } else if (showTimeCode != null && !showTimeCode.isEmpty()) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode()
                            .equals(showTimeCode))
                    .toList();
        } else if (staffId != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getStaff().getId().equals(staffId))
                    .toList();
        } else if (userId != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getUser().getId().equals(userId))
                    .toList();
        } else if (startDate != null && endDate != null) {
            invoices = invoices.stream()
                    .filter(invoice -> invoice.getCreatedDate().isAfter(startDate.atStartOfDay())
                            && invoice.getCreatedDate().isBefore(endDate.atStartOfDay().plusDays(1)))
                    .toList();
        }
        return invoices.size();
    }

    @Override
    public CinemaDto getCinemaByInvoiceId(Long id) {
        CinemaDto cinemaDto = new CinemaDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        cinemaDto.setId(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getId());
        cinemaDto.setName(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema().getName());
        AddressDto addressDto = getAddressDto(invoice);
        cinemaDto.setAddress(addressDto);
        return cinemaDto;
    }

    private static AddressDto getAddressDto(Invoice invoice) {
        AddressDto addressDto = new AddressDto();
        addressDto.setId(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema()
                .getAddress().getId());
        addressDto.setCity(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema()
                .getAddress().getCity());
        addressDto.setDistrict(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema()
                .getAddress().getDistrict());
        addressDto.setStreet(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema()
                .getAddress().getStreet());
        addressDto.setWard(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema()
                .getAddress().getWard());
        addressDto.setNation(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getCinema()
                .getAddress().getNation());
        return addressDto;
    }

    @Override
    public RoomDto getRoomByInvoiceId(Long id) {
        RoomDto roomDto = new RoomDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        roomDto.setId(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getId());
        roomDto.setName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getName());
        // lấy giá phòng từ price detail

        roomDto.setPrice(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getPriceDetails().stream()
                        .filter(priceDetail -> priceDetail.getType() == EDetailType.ROOM)
                        .findFirst()
                        .get()
                        .getPrice());
        String typeRoom = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getRoom().getType()
                .toString();
        roomDto.setType(typeRoom);
        return roomDto;
    }

    @Override
    public MovieDto getMovieByInvoiceId(Long id) {
        MovieDto movieDto = new MovieDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        movieDto.setCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCode());
        movieDto.setName(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getName());
        movieDto.setDurationMinutes(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDurationMinutes());
        movieDto.setReleaseDate(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getReleaseDate());
        movieDto.setImageLink(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getImageLink());
        movieDto.setDirector(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDirector());
        movieDto.setDirector(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDirector());
        movieDto.setProducer(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getProducer());
        movieDto.setCast(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCast());
        // convert genresdto
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
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        showTimeDto.setCode(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getCode());
        showTimeDto.setShowDate(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowDate());
        showTimeDto.setShowTime(invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getShowTime());
        return showTimeDto;
    }

    @Override
    public UserDto getUserByInvoiceId(Long id) {
        UserDto userDto = new UserDto();
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        userDto.setCode(invoice.getUser().getCode());
        userDto.setUsername(invoice.getUser().getUsername());
        userDto.setEmail(invoice.getUser().getEmail());
        userDto.setPhone(invoice.getUser().getPhone());
        return userDto;
    }

    @Override
    public List<InvoiceFoodDetailDto> getInvoiceFoodDetailByInvoiceId(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        List<InvoiceFoodDetail> invoiceFoodDetails = invoice.getInvoiceFoodDetails();
        List<InvoiceFoodDetailDto> invoiceFoodDetailDtos = new ArrayList<>();
        for (InvoiceFoodDetail invoiceFoodDetail : invoiceFoodDetails) {
            InvoiceFoodDetailDto invoiceFoodDetailDto = modelMapper.map(invoiceFoodDetail, InvoiceFoodDetailDto.class);
            invoiceFoodDetailDto.setFoodName(invoiceFoodDetail.getFood().getName());
            invoiceFoodDetailDto.setQuantity(invoiceFoodDetail.getQuantity());
            // invoiceFoodDetailDto.setPriceItem(invoiceFoodDetail.getFood().getPrice().getPrice());
            // invoiceFoodDetailDto.setPrice(invoiceFoodDetail.getPrice());
            invoiceFoodDetailDto.setNote(invoiceFoodDetail.getNote());
            invoiceFoodDetailDtos.add(invoiceFoodDetailDto);
        }
        return invoiceFoodDetailDtos;

    }

    @Override
    public List<InvoiceTicketDetailDto> getInvoiceTicketDetailByInvoiceId(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        List<InvoiceTicketDetail> invoiceTicketDetails = invoice.getInvoiceTicketDetails();
        List<InvoiceTicketDetailDto> invoiceTicketDetailDtos = new ArrayList<>();
        for (InvoiceTicketDetail invoiceTicketDetail : invoiceTicketDetails) {
            InvoiceTicketDetailDto invoiceTicketDetailDto = modelMapper.map(invoiceTicketDetail,
                    InvoiceTicketDetailDto.class);
            invoiceTicketDetailDto.setTicketCode(invoiceTicketDetail.getTicket().getCode());
            invoiceTicketDetailDto.setSeatName(invoiceTicketDetail.getTicket().getSeat().getName());
            invoiceTicketDetailDto.setRowCol(invoiceTicketDetail.getTicket().getSeat().getSeatRow() + " - "
                    + invoiceTicketDetail.getTicket().getSeat().getSeatColumn());
            String typeSeat = String.valueOf(invoiceTicketDetail.getTicket().getSeat().getSeatType().getName());
            invoiceTicketDetailDto.setSeatType(typeSeat);
            invoiceTicketDetailDto.setPrice(invoiceTicketDetail.getPrice());
            invoiceTicketDetailDto.setPriceItem(invoiceTicketDetail.getTicket().getSeat().getSeatType()
                    .getPriceDetails().stream()
                    .filter(priceDetail -> priceDetail.getType() == EDetailType.TYPE_SEAT)
                    .findFirst()
                    .get()
                    .getPrice().add(invoiceTicketDetail.getTicket().getShowTime().getRoom().getPriceDetails().stream()
                            .filter(priceDetail -> priceDetail.getType() == EDetailType.ROOM)
                            .findFirst()
                            .get()
                            .getPrice()));
            invoiceTicketDetailDto.setQuantity(invoiceTicketDetail.getQuantity());
            invoiceTicketDetailDto.setNote(invoiceTicketDetail.getNote());
            invoiceTicketDetailDtos.add(invoiceTicketDetailDto);
        }
        return invoiceTicketDetailDtos;
    }

    @Override
    public List<PromotionLineDto> getPromotionLineByInvoiceId(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy hóa đơn với id: " + id, HttpStatus.NOT_FOUND));
        List<PromotionLine> promotionLines = new ArrayList<>(invoice.getPromotionLines());
        return promotionLines.stream()
                .map(promotionLine -> {
                    PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);

                    if (promotionLine.getTypePromotion().equals(ETypePromotion.DISCOUNT)) {
                        PromotionDiscountDetailDto promotionDiscountDetailDto = modelMapper
                                .map(promotionLine.getPromotionDiscountDetail(), PromotionDiscountDetailDto.class);
                        promotionLineDto.setPromotionDiscountDetailDto(promotionDiscountDetailDto);
                    }
                    if (promotionLine.getTypePromotion().equals(ETypePromotion.FOOD)) {
                        PromotionFoodDetailDto promotionFoodDetailDto = modelMapper
                                .map(promotionLine.getPromotionFoodDetail(), PromotionFoodDetailDto.class);
                        promotionLineDto.setPromotionFoodDetailDto(promotionFoodDetailDto);
                    }
                    if (promotionLine.getTypePromotion().equals(ETypePromotion.TICKET)) {
                        // lấy promotion ticket detail
                        PromotionTicketDetailDto promotionTicketDetailDto = modelMapper
                                .map(promotionLine.getPromotionTicketDetail(), PromotionTicketDetailDto.class);
                        promotionLineDto.setPromotionTicketDetailDto(promotionTicketDetailDto);
                    }
                    return promotionLineDto;
                })
                .toList();
    }

    @Override
    public void removePromotionLineFromInvoice(Long invoiceId, Long promotionLineId) {
        Invoice invoice = findById(invoiceId);
        // nếu không có chương trình khuyến mãi nào trong hóa đơn thì không thể xóa
        PromotionLine promotionLine = promotionLineService.findById(promotionLineId);
        List<PromotionLine> promotionLines = invoice.getPromotionLines();
        // hoàn lại số lượng của từng khuyến mã có trong hóa đơn
        promotionLines.remove(promotionLine);
        promotionLines
                .forEach(promotionLine1 -> promotionLineService.updateQuantityPromotionLine(promotionLine1.getId(), 1));

        invoice.setPromotionLines(promotionLines);
        invoiceRepository.save(invoice);
        clear();
    }

    @Override
    public List<Invoice> getInvoiceByPromotionLineId(Long promotionLineId) {
        PromotionLine promotionLine = promotionLineService.findById(promotionLineId);
        return invoiceRepository.findByPromotionLineId(promotionLine.getId());

    }

    private String randomCode() {
        return "HD" + LocalDateTime.now().getNano();
    }

    private String constructEmailContent(Invoice invoice) {
        // Create HTML content for the email
        StringBuilder contentBuilder = new StringBuilder();
        String name = invoice.getUser().getUsername();
        String code = invoice.getCode();
        // chỉ lấy ngày thắng năm đặt
        LocalDate showDate = invoice.getCreatedDate().toLocalDate();
        String createdDate = showDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        BigDecimal totalPrice = invoice.getTotalPrice();
        // định dạng tiền tệ
        DecimalFormat formatter = new DecimalFormat("###,###,###");
        String total = formatter.format(totalPrice) + " Vnd";
        String typePayment = invoice.getTypePay().toString();
        // nếu là CASH thì là tiền mặt
        if (typePayment.equals("CASH")) {
            typePayment = "Tiền mặt";
        }
        String movieName = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getName();
        String durationMinutes = String.valueOf(
                invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDurationMinutes());
        String country = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCountry();
        String director = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getDirector();
        // diễn viên cách nhau bở dấu phẩy khi cast có dạng " aaa, bbbb, ccc"
        String cast = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getCast();
        String imageLink = invoice.getInvoiceTicketDetails().get(0).getTicket().getShowTime().getMovie().getImageLink();
        // Add more invoice details here
        contentBuilder.append(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional //EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
                        +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">"
                        +
                        "<head>" +
                        "<!--[if gte mso 9]>" +
                        "<xml>" +
                        "  <o:OfficeDocumentSettings>" +
                        "    <o:AllowPNG/>" +
                        "    <o:PixelsPerInch>96</o:PixelsPerInch>" +
                        "  </o:OfficeDocumentSettings>" +
                        "</xml>" +
                        "<![endif]-->" +
                        "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                        "  <meta name=\"x-apple-disable-message-reformatting\">" +
                        "  <!--[if !mso]><!--><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><!--<![endif]-->"
                        +
                        "  <title></title>" +
                        "  " +
                        "    <style type=\"text/css\">" +
                        "      @media only screen and (min-width: 520px) {" +
                        "  .u-row {" +
                        "    width: 500px !important;" +
                        "  }" +
                        "  .u-row .u-col {" +
                        "    vertical-align: top;" +
                        "  }" +
                        "" +
                        "  .u-row .u-col-45 {" +
                        "    width: 225px !important;" +
                        "  }" +
                        "" +
                        "  .u-row .u-col-50 {" +
                        "    width: 250px !important;" +
                        "  }" +
                        "" +
                        "  .u-row .u-col-55 {" +
                        "    width: 275px !important;" +
                        "  }" +
                        "" +
                        "  .u-row .u-col-100 {" +
                        "    width: 500px !important;" +
                        "  }" +
                        "" +
                        "}" +
                        "" +
                        "@media (max-width: 520px) {" +
                        "  .u-row-container {" +
                        "    max-width: 100% !important;" +
                        "    padding-left: 0px !important;" +
                        "    padding-right: 0px !important;" +
                        "  }" +
                        "  .u-row .u-col {" +
                        "    min-width: 320px !important;" +
                        "    max-width: 100% !important;" +
                        "    display: block !important;" +
                        "  }" +
                        "  .u-row {" +
                        "    width: 100% !important;" +
                        "  }" +
                        "  .u-col {" +
                        "    width: 100% !important;" +
                        "  }" +
                        "  .u-col > div {" +
                        "    margin: 0 auto;" +
                        "  }" +
                        "}" +
                        "body {" +
                        "  margin: 0;" +
                        "  padding: 0;" +
                        "}" +
                        "" +
                        "table," +
                        "tr," +
                        "td {" +
                        "  vertical-align: top;" +
                        "  border-collapse: collapse;" +
                        "}" +
                        "" +
                        "p {" +
                        "  margin: 0;" +
                        "}" +
                        "" +
                        ".ie-container table," +
                        ".mso-container table {" +
                        "  table-layout: fixed;" +
                        "}" +
                        "" +
                        "* {" +
                        "  line-height: inherit;" +
                        "}" +
                        "" +
                        "a[x-apple-data-detectors='true'] {" +
                        "  color: inherit !important;" +
                        "  text-decoration: none !important;" +
                        "}" +
                        "" +
                        "table, td { color: #000000; } </style>" +
                        "  " +
                        "  " +
                        "" +
                        "<!--[if !mso]><!--><link href=\"https://fonts.googleapis.com/css?family=Montserrat:400,700&display=swap\" rel=\"stylesheet\" type=\"text/css\"><!--<![endif]-->"
                        +
                        "" +
                        "</head>" +
                        "" +
                        "<body class=\"clean-body u_body\" style=\"margin: 0;padding: 0;-webkit-text-size-adjust: 100%;background-color: #ffffff;color: #000000\">"
                        +
                        "  <!--[if IE]><div class=\"ie-container\"><![endif]-->" +
                        "  <!--[if mso]><div class=\"mso-container\"><![endif]-->" +
                        "  <table style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;min-width: 320px;Margin: 0 auto;background-color: #ffffff;width:100%\" cellpadding=\"0\" cellspacing=\"0\">"
                        +
                        "  <tbody>" +
                        "  <tr style=\"vertical-align: top\">" +
                        "    <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top\">"
                        +
                        "    <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color: #ffffff;\"><![endif]-->"
                        +
                        "    " +
                        "  " +
                        "  " +
                        "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">" +
                        "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 500px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">"
                        +
                        "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">"
                        +
                        "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:500px;\"><tr style=\"background-color: transparent;\"><![endif]-->"
                        +
                        "      " +
                        "<!--[if (mso)|(IE)]><td align=\"center\" width=\"500\" style=\"background-color: #f7fbfc;width: 500px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->"
                        +
                        "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 500px;display: table-cell;vertical-align: top;\">"
                        +
                        "  <div style=\"background-color: #f7fbfc;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">"
                        +
                        "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->"
                        +
                        "  " +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "  <div style=\"font-size: 14px; line-height: 140%; text-align: center; word-wrap: break-word;\">"
                        +
                        "    <p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 30px; line-height: 42px; font-family: Montserrat, sans-serif; color: #e03e2d;\"><strong><span style=\"line-height: 19.6px;\">InfinityCinema</span></strong></span></p>"
                        +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 22px; line-height: 30.8px; font-family: Montserrat, sans-serif; color: #000000;\"><strong><span style=\"line-height: 19.6px;\">Đặt vé thành công</span></strong></span><span style=\"font-size: 22px; line-height: 30.8px; font-family: Montserrat, sans-serif; color: #000000;\"><span style=\"line-height: 19.6px;\"></span></span></p>"
                        +
                        "<p style=\"font-size: 14px; line-height: 140%; text-align: left;\"><span style=\"font-size: 14px; line-height: 19.6px; font-family: Montserrat, sans-serif; color: #000000;\"><span style=\"line-height: 19.6px;\">Cảm ơn <strong>")
                .append(name)
                .append("</strong> đã chọn hệ thống rạp chiếu phim InfinityCinema, chúc bạn và bạn bè, người thân có buổi xem phim vui vẻ\uD83D\uDE0A\uD83D\uDE0A\uD83D\uDE0A</span></span><span style=\"font-size: 14px; line-height: 19.6px; font-family: Montserrat, sans-serif; color: #000000;\"><span style=\"line-height: 19.6px;\"></span></span></p>"
                        +
                        "<p style=\"font-size: 14px; line-height: 140%; text-align: left;\"> </p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 18px; line-height: 25.2px; font-family: Montserrat, sans-serif;\"><strong><span style=\"line-height: 25.2px; font-size: 18px;\">Thông tin hóa đơn</span></strong></span></p>"
                        +
                        "  </div>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "  <table height=\"0px\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse: collapse;table-layout: fixed;border-spacing: 0;mso-table-lspace: 0pt;mso-table-rspace: 0pt;vertical-align: top;border-top: 2px solid #e7e7e7;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">"
                        +
                        "    <tbody>" +
                        "      <tr style=\"vertical-align: top\">" +
                        "        <td style=\"word-break: break-word;border-collapse: collapse !important;vertical-align: top;font-size: 0px;line-height: 0px;mso-line-height-rule: exactly;-ms-text-size-adjust: 100%;-webkit-text-size-adjust: 100%\">"
                        +
                        "          <span>&#160;</span>" +
                        "        </td>" +
                        "      </tr>" +
                        "    </tbody>" +
                        "  </table>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->" +
                        "  </div>" +
                        "</div>" +
                        "<!--[if (mso)|(IE)]></td><![endif]-->" +
                        "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->" +
                        "    </div>" +
                        "  </div>" +
                        "  </div>" +
                        "  " +
                        "" +
                        "" +
                        "  " +
                        "  " +
                        "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">" +
                        "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 500px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">"
                        +
                        "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">"
                        +
                        "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:500px;\"><tr style=\"background-color: transparent;\"><![endif]-->"
                        +
                        "      " +
                        "<!--[if (mso)|(IE)]><td align=\"center\" width=\"225\" style=\"width: 225px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->"
                        +
                        "<div class=\"u-col u-col-45\" style=\"max-width: 320px;min-width: 225px;display: table-cell;vertical-align: top;\">"
                        +
                        "  <div style=\"height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">"
                        +
                        "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->"
                        +
                        "  " +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "  <div style=\"font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;\">"
                        +
                        "    <p style=\"font-size: 14px; line-height: 140%;\"><strong><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\">Mã hóa đơn:</span></strong><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\">")
                .append(code)
                .append("</span><br /><strong><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\">Ngày đặt:</span></strong><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\">")
                .append(createdDate).append("</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"> </p>" +
                        "  </div>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->" +
                        "  </div>" +
                        "</div>" +
                        "<!--[if (mso)|(IE)]></td><![endif]-->" +
                        "<!--[if (mso)|(IE)]><td align=\"center\" width=\"275\" style=\"width: 275px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->"
                        +
                        "<div class=\"u-col u-col-55\" style=\"max-width: 320px;min-width: 275px;display: table-cell;vertical-align: top;\">"
                        +
                        "  <div style=\"height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">"
                        +
                        "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->"
                        +
                        "  " +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "  <div style=\"font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;\">"
                        +
                        "    <p style=\"font-size: 14px; line-height: 140%;\"><strong><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\">Tổng tiền: </span></strong><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\">")
                .append(total).append("</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-family: Montserrat, sans-serif; font-size: 14px; line-height: 19.6px;\"><strong>Phương thức thanh toán: </strong>")
                .append(typePayment).append("</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"> </p>" +
                        "  </div>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->" +
                        "  </div>" +
                        "</div>" +
                        "<!--[if (mso)|(IE)]></td><![endif]-->" +
                        "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->" +
                        "    </div>" +
                        "  </div>" +
                        "  </div>" +
                        "  " +
                        "" +
                        "" +
                        "  " +
                        "  " +
                        "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">" +
                        "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 500px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">"
                        +
                        "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">"
                        +
                        "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:500px;\"><tr style=\"background-color: transparent;\"><![endif]-->"
                        +
                        "      " +
                        "<!--[if (mso)|(IE)]><td align=\"center\" width=\"250\" style=\"width: 250px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->"
                        +
                        "<div class=\"u-col u-col-50\" style=\"max-width: 320px;min-width: 250px;display: table-cell;vertical-align: top;\">"
                        +
                        "  <div style=\"height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">"
                        +
                        "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->"
                        +
                        "  " +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:0px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">" +
                        "  <tr>" +
                        "    <td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\">" +
                        "      " +
                        "      <img align=\"center\" border=\"0\" src=\"")
                .append(imageLink)
                .append("\" alt=\"Hand Bag\" title=\"Hand Bag\" style=\"outline: none;text-decoration: none;-ms-interpolation-mode: bicubic;clear: both;display: inline-block !important;border: none;height: auto;float: none;width: 85%;max-width: 212.5px;\" width=\"212.5\"/>"
                        +
                        "      " +
                        "    </td>" +
                        "  </tr>" +
                        "</table>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->" +
                        "  </div>" +
                        "</div>" +
                        "<!--[if (mso)|(IE)]></td><![endif]-->" +
                        "<!--[if (mso)|(IE)]><td align=\"center\" width=\"250\" style=\"background-color: #ffffff;width: 250px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->"
                        +
                        "<div class=\"u-col u-col-50\" style=\"max-width: 320px;min-width: 250px;display: table-cell;vertical-align: top;\">"
                        +
                        "  <div style=\"background-color: #ffffff;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">"
                        +
                        "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->"
                        +
                        "  " +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:30px 10px 66px 20px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "  <div style=\"font-size: 14px; line-height: 140%; text-align: left; word-wrap: break-word;\">"
                        +
                        "    <p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 18px; line-height: 25.2px;\"><span style=\"font-family: Montserrat, sans-serif;\"><strong>")
                .append(movieName).append("</strong></span></span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 16px; line-height: 22.4px;\">Thời lượng: ")
                .append(durationMinutes).append(" phút</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 16px; line-height: 22.4px;\">Quốc gia: ")
                .append(country).append("</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 16px; line-height: 22.4px;\">Đạo diễn: ")
                .append(director).append("</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 16px; line-height: 22.4px;\">Diễn viên: ")
                .append(cast).append("</span></p>" +
                        "<p style=\"font-size: 14px; line-height: 140%;\"> </p>" +
                        "  </div>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->" +
                        "  </div>" +
                        "</div>" +
                        "<!--[if (mso)|(IE)]></td><![endif]-->" +
                        "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->" +
                        "    </div>" +
                        "  </div>" +
                        "  </div>" +
                        "  " +
                        "" +
                        "" +
                        "  " +
                        "  " +
                        "<div class=\"u-row-container\" style=\"padding: 0px;background-color: transparent\">" +
                        "  <div class=\"u-row\" style=\"margin: 0 auto;min-width: 320px;max-width: 500px;overflow-wrap: break-word;word-wrap: break-word;word-break: break-word;background-color: transparent;\">"
                        +
                        "    <div style=\"border-collapse: collapse;display: table;width: 100%;height: 100%;background-color: transparent;\">"
                        +
                        "      <!--[if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding: 0px;background-color: transparent;\" align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:500px;\"><tr style=\"background-color: transparent;\"><![endif]-->"
                        +
                        "      " +
                        "<!--[if (mso)|(IE)]><td align=\"center\" width=\"500\" style=\"background-color: #f7fbfc;width: 500px;padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\" valign=\"top\"><![endif]-->"
                        +
                        "<div class=\"u-col u-col-100\" style=\"max-width: 320px;min-width: 500px;display: table-cell;vertical-align: top;\">"
                        +
                        "  <div style=\"background-color: #f7fbfc;height: 100%;width: 100% !important;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\">"
                        +
                        "  <!--[if (!mso)&(!IE)]><!--><div style=\"box-sizing: border-box; height: 100%; padding: 0px;border-top: 0px solid transparent;border-left: 0px solid transparent;border-right: 0px solid transparent;border-bottom: 0px solid transparent;border-radius: 0px;-webkit-border-radius: 0px; -moz-border-radius: 0px;\"><!--<![endif]-->"
                        +
                        "  " +
                        "<table style=\"font-family:arial,helvetica,sans-serif;\" role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" border=\"0\">"
                        +
                        "  <tbody>" +
                        "    <tr>" +
                        "      <td style=\"overflow-wrap:break-word;word-break:break-word;padding:20px 10px 10px;font-family:arial,helvetica,sans-serif;\" align=\"left\">"
                        +
                        "        " +
                        "  <div style=\"font-size: 14px; line-height: 140%; text-align: center; word-wrap: break-word;\">"
                        +
                        "    <p style=\"font-size: 14px; line-height: 140%;\"><span style=\"font-size: 24px; line-height: 33.6px;\"><strong><span style=\"font-family: Montserrat, sans-serif;\">Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi</span></strong></span></p>"
                        +
                        "  </div>" +
                        "" +
                        "      </td>" +
                        "    </tr>" +
                        "  </tbody>" +
                        "</table>" +
                        "" +
                        "  <!--[if (!mso)&(!IE)]><!--></div><!--<![endif]-->" +
                        "  </div>" +
                        "</div>" +
                        "<!--[if (mso)|(IE)]></td><![endif]-->" +
                        "      <!--[if (mso)|(IE)]></tr></table></td></tr></table><![endif]-->" +
                        "    </div>" +
                        "  </div>" +
                        "  </div>" +
                        "  " +
                        "" +
                        "" +
                        "    <!--[if (mso)|(IE)]></td></tr></table><![endif]-->" +
                        "    </td>" +
                        "  </tr>" +
                        "  </tbody>" +
                        "  </table>" +
                        "  <!--[if mso]></div><![endif]-->" +
                        "  <!--[if IE]></div><![endif]-->" +
                        "</body>" +
                        "" +
                        "</html>");
        return contentBuilder.toString();
    }
}
