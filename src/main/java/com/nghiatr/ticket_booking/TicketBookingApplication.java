package com.nghiatr.ticket_booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TicketBookingApplication {

    /**
     * Điểm khởi chạy chính của ứng dụng Spring Boot Ticket Booking.
     *
     * @param args các tham số dòng lệnh truyền vào khi khởi động ứng dụng
     */
	public static void main(String[] args) {
		SpringApplication.run(TicketBookingApplication.class, args);
	}

}
