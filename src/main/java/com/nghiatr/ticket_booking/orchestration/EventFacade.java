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

import com.nghiatr.ticket_booking.shared.storage.FileStorageService;
import com.nghiatr.ticket_booking.shared.storage.FileStorageValidator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorageService fileStorageService;
    private final FileStorageValidator fileStorageValidator;

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
        return update(eventId, updateData, null);
    }

    @Transactional
    public EventItemResponse update(
            UUID eventId,
            EventUpdateRequest updateData,
            MultipartFile image
    ) {
        Event event = eventService.getOrganizerEvent(eventId);
        List<TicketClass> ticketClasses = ticketClassService.getAllByEvent(event);

        if (updateData != null) {
            eventActionValidator.validate(
                    event,
                    ticketClasses,
                    updateData,
                    EventActionType.UPDATE
            );
        }

        boolean hasNewImage = image != null && !image.isEmpty();
        String oldImageUrl = event.getEventImgUrl();
        String newImageUrl = null;

        if (hasNewImage) {
            fileStorageValidator.validateImageFile(image);
            newImageUrl = fileStorageService.uploadFile(image, "events");
            event.setEventImgUrl(newImageUrl);

            final String finalNewImageUrl = newImageUrl;
            final String finalOldImageUrl = oldImageUrl;

            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                            fileStorageService.deleteFile(finalNewImageUrl);
                        }
                    }

                    @Override
                    public void afterCommit() {
                        if (finalOldImageUrl != null && !finalOldImageUrl.isBlank()) {
                            fileStorageService.deleteFile(finalOldImageUrl);
                        }
                    }
                });
            }
        }

        try {
            if (updateData != null) {
                if (updateData.ticketClasses() != null
                        && !updateData.ticketClasses().isEmpty()) {
                    // Sử dụng service của ticketClass để chỉnh sửa thông tin.
                    ticketClassService.editTicketClasses(eventId, updateData.ticketClasses());
                }

                eventService.updateEventFields(event, updateData);
            }

            eventService.saveEvent(event);
            return EventItemResponse.from(event);
        } catch (Exception ex) {
            if (hasNewImage && newImageUrl != null) {
                fileStorageService.deleteFile(newImageUrl);
            }
            throw ex;
        }
    }
}
