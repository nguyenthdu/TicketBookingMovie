package com.app.TicketBookingMovie.services;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

public interface VNPAYService {
   String createOrder(HttpServletRequest request, int amount, Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId);
    int orderReturn(HttpServletRequest request);
}
