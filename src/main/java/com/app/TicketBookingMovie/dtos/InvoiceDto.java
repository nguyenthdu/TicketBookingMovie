package com.app.TicketBookingMovie.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class InvoiceDto implements Serializable {
    private  Long id;
    private String code;
    private double totalPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdDate;
    private boolean status;
    private String showTimeCode;
    private Long cinemaId;
    private String cinemaName;
    private Long roomId;
    private String roomName;
    private Long movieId;
    private String movieImage;
    private String movieName;
    private Long userId;
    private String userName;
    private Long staffId;
    private String staffName;

}
