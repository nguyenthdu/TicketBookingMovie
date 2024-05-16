package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.config.payments.VNPayConfig;
import com.app.TicketBookingMovie.services.InvoiceService;
import com.app.TicketBookingMovie.services.ShowTimeService;
import com.app.TicketBookingMovie.services.VNPAYService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPAYServiceImpl implements VNPAYService {
    private final InvoiceService invoiceService;
    private final ShowTimeService showTimeService;

    public VNPAYServiceImpl(InvoiceService invoiceService, ShowTimeService showTimeService) {
        this.invoiceService = invoiceService;
        this.showTimeService = showTimeService;
    }

    @Override
    public String createOrder(HttpServletRequest request, int amount, Long showTimeId, Set<Long> seatIds,
            List<Long> foodIds, String emailUser, Long staffId) {
        // Các bạn có thể tham khảo tài liệu hướng dẫn và điều chỉnh các tham số
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);
        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
        String orderType = "order-type";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        // lưu các thông tin vào orderInfo
        StringBuilder orderInfo = new StringBuilder();
        orderInfo.append(showTimeId).append(";");
        for (Long seatId : seatIds) {
            orderInfo.append(seatId).append(",");
        }
        orderInfo.append(";");
        if (foodIds.isEmpty()) {
            orderInfo.append(" ,");
        } else {
            for (Long foodId : foodIds) {
                orderInfo.append(foodId).append(",");
            }
        }

        orderInfo.append(";");
        orderInfo.append(emailUser).append(";");
        orderInfo.append(staffId);
        vnp_Params.put("vnp_OrderInfo", orderInfo.toString());
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "13.212.15.8");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String salt = VNPayConfig.secretKey;
        String vnp_SecureHash = VNPayConfig.hmacSHA512(salt, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
        return paymentUrl;
    }

    public int orderReturn(HttpServletRequest request) {
        Map fields = new HashMap();
        for (Enumeration params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = null;
            String fieldValue = null;
            try {
                fieldName = URLEncoder.encode((String) params.nextElement(), StandardCharsets.US_ASCII.toString());
                fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }
        String signValue = VNPayConfig.hashAllFields(fields);
        // lấy thông tin từ request để tạo hóa đơn
        Long showTimeId = Long.parseLong(request.getParameter("vnp_OrderInfo").split(";")[0]);
        Set<Long> seatIds = new HashSet<>();
        String[] seatIdStr = request.getParameter("vnp_OrderInfo").split(";")[1].split(",");
        for (String s : seatIdStr) {
            seatIds.add(Long.parseLong(s));
        }
        List<Long> foodIds = new ArrayList<>();
        String[] foodIdStr = request.getParameter("vnp_OrderInfo").split(";")[2].split(",");
        for (String s : foodIdStr) {
            if (!s.equals(" ")) {
                foodIds.add(Long.parseLong(s));
            }
        }
        String emailUser = request.getParameter("vnp_OrderInfo").split(";")[3];
        Long staffId = Long.parseLong(request.getParameter("vnp_OrderInfo").split(";")[4]);
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                invoiceService.createInvoice(showTimeId, seatIds, foodIds, emailUser, staffId, "VNPAY");
                return 1;
            } else {
                showTimeService.updateStatusHoldSeat(seatIds, showTimeId, true);
                return 0;
            }
        } else {
            return -1;
        }
    }

}