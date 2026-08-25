package com.nghiatr.ticket_booking.seat.service;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.seat.entity.Seat;
import com.nghiatr.ticket_booking.seat.entity.SeatBlock;
import com.nghiatr.ticket_booking.seat.entity.SeatLayout;
import com.nghiatr.ticket_booking.seat.entity.SeatStatus;
import com.nghiatr.ticket_booking.seat.repository.SeatRepository;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import com.nghiatr.ticket_booking.ticketClass.service.TicketClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    private final TicketClassService ticketClassService;

    public void deleteAllByEvent(Event event) {
        seatRepository.deleteAllByEventId(event);
    }

    @Transactional
    public int createAndSaveSeat(
            Event event,
            SeatLayout seatLayout
    ) {
        List<Seat> seats = new ArrayList<>();

        for(SeatBlock block : seatLayout.getSeatLayout()) {
            if(block.getRows() == 0 || block.getCols() == 0) {
                continue;
            }

            TicketClass ticketClass = ticketClassService.getById(block.getTicketClassId());

            Set<String> deletedSeats = block.getDeletedSeats()
                    .stream()
                    .map(seat ->
                            seat.getRow() + "-" + seat.getCol())
                    .collect(Collectors.toSet());

            for(int row = 1; row <= block.getRows(); row++) {
                for(int col = 1; col <= block.getCols(); col++) {

                    String coordinate = row + "-" + col;

                    if(deletedSeats.contains(coordinate)) {
                        continue;
                    }

                    Seat seat = Seat.builder()
                            .eventId(event)
                            .ticketClassId(ticketClass)
                            .name(
                                    block.getBlockId()
                                            + "-R" + row
                                            + "-L" + col
                            )
                            .status(SeatStatus.AVAILABLE)
                            .build();
                    seats.add(seat);
                }
            }
        }

        // Bước 4: Insert hàng loạt ghế vào db
        if(!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }

        return seats.size();
    }
}
