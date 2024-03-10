package com.app.TicketBookingMovie.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
	private List<T> content;
	private int totalPages;
	private long totalElements;
	private int currentPage;
	private int pageSize;
}