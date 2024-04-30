package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.EPay;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    //nhân viên thanh toán
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User staff;
    @ManyToMany
    @JoinTable(name = "invoice_promotion_line",
            joinColumns = @JoinColumn(name = "invoice_id"),
            inverseJoinColumns = @JoinColumn(name = "promotion_line_id"))
    private Set<PromotionLine> promotionLines;
    @Enumerated(EnumType.STRING)
    private EPay typePay;
    private boolean status;
}
