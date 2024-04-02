package com.app.TicketBookingMovie.services.impl;


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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, TicketService ticketService, FoodService foodService, UserService userService, PriceDetailRepository priceDetailRepository, PromotionLineRepository promotionLineRepository) {
        this.invoiceRepository = invoiceRepository;
        this.ticketService = ticketService;
        this.foodService = foodService;
        this.userService = userService;
        this.priceDetailRepository = priceDetailRepository;
        this.promotionLineRepository = promotionLineRepository;
    }

    @Override
    @Transactional
    public void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId, Set<Long> promotionLineIds) {
        // Tạo một đối tượng Invoice mới
        Invoice invoice = new Invoice();
        invoice.setCode(randomCode()); // Tạo mã hóa đơn
        invoice.setCreatedDate(LocalDateTime.now());
        invoice.setStatus(false); // Chưa thanh toán

        // Tạo danh sách chi tiết vé từ thông tin vé
        List<InvoiceTicketDetail> invoiceTicketDetails = new ArrayList<>();
        List<Ticket> tickets = ticketService.createTickets(showTimeId, seatIds);
        for (Ticket ticket : tickets) {
            InvoiceTicketDetail ticketDetail = new InvoiceTicketDetail();
            ticketDetail.setTicket(ticket);
            ticketDetail.setQuantity(1); // Mỗi vé là 1 sản phẩm
            ticketDetail.setPrice(ticket.getPrice()); // Giá vé
            invoiceTicketDetails.add(ticketDetail);
        }
        invoice.setInvoiceTicketDetails(invoiceTicketDetails);
        LocalDateTime currentTime = LocalDateTime.now();
        List<PriceDetail> currentPriceDetails = priceDetailRepository.findCurrentSalePriceDetails(currentTime);

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
            InvoiceFoodDetail foodDetail = new InvoiceFoodDetail();
            foodDetail.setFood(food);
            foodDetail.setQuantity(quantity);
            Optional<PriceDetail> salePriceDetail = currentPriceDetails.stream()
                    .filter(salePrice -> salePrice.getFood().getId().equals(foodId))
                    .findFirst();
            if (salePriceDetail.isPresent()) {
                foodDetail.setPrice(salePriceDetail.get().getPrice() * quantity);
            } else {
                foodDetail.setPrice(food.getPrice() * quantity);
            }
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



    private String randomCode() {
        return "HD" + LocalDateTime.now().getNano();
    }
}
