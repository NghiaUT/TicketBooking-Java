package com.nghiatr.ticket_booking.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Tên sự kiện không được để trống")
    private String eventName;

    @NotNull(message = "Sự kiện phải thuộc một địa điểm")
    private UUID venueId;

    @NotBlank(message = "Sự kiện ít nhất phải thuộc một thể loại")
    private String genre;

    @NotBlank(message = "Mô tả sự kiện không được để trống")
    private String description;

    @NotNull(message = "Sự kiện phải có thời gian bắt đầu")
    private LocalDateTime timeToStart;

    private LocalDateTime dateToStart;

    private LocalDateTime timeToRelease;

    private String duration;

    private String eventImgUrl;
}
