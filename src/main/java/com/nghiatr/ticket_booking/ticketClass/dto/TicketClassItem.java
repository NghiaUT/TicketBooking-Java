package com.nghiatr.ticket_booking.ticketClass.dto;

import com.nghiatr.ticket_booking.ticketClass.entity.TicketClassType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketClassItem {

    private String id;

    @NotBlank(message = "Hạng vé phải thuộc về một Event")
    private UUID eventId;

    @NotBlank(message = "Hạng vé phải có tên")
    private String className;

    private double price;

    private int quota;

    private TicketClassType type;

    private String color;

    private String description;
}