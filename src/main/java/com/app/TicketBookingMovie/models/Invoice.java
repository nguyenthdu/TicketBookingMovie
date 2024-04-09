package com.app.TicketBookingMovie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private BigDecimal totalPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceFoodDetail> invoiceFoodDetails;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceTicketDetail> invoiceTicketDetails;
    private LocalDateTime createdDate;
    private LocalDateTime cancelledDate;
    //nhân viên thanh toán
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User staff;
    @ManyToOne
    @JsonIgnore
    private Promotion  promotion;
    private boolean status;
}
