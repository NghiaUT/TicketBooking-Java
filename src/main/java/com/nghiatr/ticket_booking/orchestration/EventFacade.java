package com.nghiatr.ticket_booking.orchestration;

import com.nghiatr.ticket_booking.event.dto.EventItemResponse;
import com.nghiatr.ticket_booking.event.dto.EventUpdateRequest;
import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventActionType;
import com.nghiatr.ticket_booking.event.service.EventService;
import com.nghiatr.ticket_booking.event.validation.EventActionValidator;
import com.nghiatr.ticket_booking.seat.dto.CreateLayoutRequest;
import com.nghiatr.ticket_booking.seat.dto.SeatLayoutResponse;
import com.nghiatr.ticket_booking.seat.entity.SeatLayout;
import com.nghiatr.ticket_booking.seat.service.SeatService;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import com.nghiatr.ticket_booking.ticketClass.service.TicketClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventFacade {
    // Sử dụng facade để tách các API riêng biệt và cần nhiều service hoạt động.
    private final TicketClassService ticketClassService;
    private final SeatService seatService;
    private final EventService eventService;
    private final EventActionValidator eventActionValidator;

    @Transactional
    public SeatLayoutResponse createLayout(
            UUID eventId,
            CreateLayoutRequest request
    ) {
        // BƯỚC 1: Kiểm tra Event
        Event event = eventService.getOrganizerEvent(eventId);

        // BƯỚC 2: Lưu layout JSON vào Event
        // Hủy tất cả ghế trước đó của event.
        seatService.deleteAllByEvent(event);
        // Tạo mới object seatLayout để lưu vào Event.
        SeatLayout seatLayout = request.toSeatLayout();
        event.setSeatLayoutMap(seatLayout);
        eventService.saveEvent(event);
        // Gọi seatService để tạo ghế và lưu vào DB.
        return new SeatLayoutResponse(seatService.createAndSaveSeat(event, seatLayout));
    }

    @Transactional
    public EventItemResponse update(
            UUID eventId,
            EventUpdateRequest updateData
    ) {
        Event event = eventService.getOrganizerEvent(eventId);
        List<TicketClass> ticketClasses = ticketClassService.getAllByEvent(event);

        eventActionValidator.validate(
                event,
                ticketClasses,
                updateData,
                EventActionType.UPDATE
        );

        if(updateData.ticketClasses() != null
                && !updateData.ticketClasses().isEmpty()) {
            // Sử dụng service của ticketClass để chỉnh sửa thông tin.
            ticketClassService.editTicketClasses(eventId, updateData.ticketClasses());
        }

        eventService.updateEventFields(event, updateData);

        return EventItemResponse.from(event);
    }
}
