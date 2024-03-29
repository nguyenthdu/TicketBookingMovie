package com.app.TicketBookingMovie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private double totalPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceDetail> invoiceDetails;
    private LocalDateTime createdDate;
    private LocalDateTime cancelledDate;
    //nhân viên thanh toán
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User staff;
    private double VAT;
    private boolean status;
}
