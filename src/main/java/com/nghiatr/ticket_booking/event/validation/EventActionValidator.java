package com.nghiatr.ticket_booking.event.validation;

import com.nghiatr.ticket_booking.event.dto.EventUpdateRequest;
import com.nghiatr.ticket_booking.event.dto.TicketClassUpdateRequest;
import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventActionType;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class EventActionValidator {
    public void validate(
            Event currentEvent,
            List<TicketClass> ticketClasses,
            EventUpdateRequest updateData,
            EventActionType actionType
    ) {
        validateLayout(currentEvent, actionType);
        validateVenue(currentEvent, updateData);
        validateStatus(currentEvent, updateData);
        validateQuota(currentEvent, updateData, ticketClasses);
    }

    private void validateLayout(
            Event event,
            EventActionType actionType
    ) {
        if (actionType == EventActionType.CREATE_LAYOUT
                && event.getSeatLayoutMap() != null) {

            throw new AppException(
                    EventErrorCode.LAYOUT_ALREADY_CREATED
            );
        }
    }

    private void validateVenue(
            Event event,
            EventUpdateRequest request
    ) {
        if (request.venueId() == null) {
            return;
        }

        UUID oldVenueId = event.getVenue().getVenueId();

        if (!request.venueId().equals(oldVenueId)
                && event.getSeatLayoutMap() != null) {

            throw new AppException(
                    EventErrorCode.VENUE_CANNOT_BE_CHANGED
            );
        }
    }

    private void validateStatus(
            Event event,
            EventUpdateRequest request
    ) {
        EventStatus status = event.getStatus();

        Set<EventStatus> lockedStatuses = Set.of(
                EventStatus.IN_PROGRESS,
                EventStatus.CANCELLED,
                EventStatus.ENDED
        );

        if (!lockedStatuses.contains(status)) {
            return;
        }

        boolean isCancelledAction =
                status == EventStatus.CANCELLED
                        && request.status() == EventStatus.CANCELLED
                        && request.onlyContainsStatus();

        if (!isCancelledAction) {
            throw new AppException(
                    EventErrorCode.EVENT_LOCKED
            );
        }
    }

    private void validateQuota(
            Event event,
            EventUpdateRequest request,
            List<TicketClass> ticketClasses
    ) {
        if (event.getStatus() != EventStatus.APPROVED
                || request.ticketClasses() == null) {
            return;
        }

        for (TicketClassUpdateRequest newClass : request.ticketClasses()) {

            ticketClasses
                    .stream()
                    .filter(oldClass ->
                            oldClass.getTicketClassId()
                                    .equals(newClass.ticketClassId()))
                    .findFirst()
                    .ifPresent(oldClass -> {

                        if (newClass.quota() < oldClass.getQuota()) {
                            throw new AppException(
                                    EventErrorCode.QUOTA_CANNOT_DECREASE
                            );
                        }
                    });
        }
    }
}
