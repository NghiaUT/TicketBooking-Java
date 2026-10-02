package com.nghiatr.ticket_booking.orchestration;

import com.nghiatr.ticket_booking.event.dto.EventItemResponse;
import com.nghiatr.ticket_booking.event.dto.EventUpdateRequest;
import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventActionType;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.event.service.EventService;
import com.nghiatr.ticket_booking.event.validation.EventActionValidator;
import com.nghiatr.ticket_booking.seat.service.SeatService;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.shared.storage.FileStorageService;
import com.nghiatr.ticket_booking.shared.storage.FileStorageValidator;
import com.nghiatr.ticket_booking.ticketClass.service.TicketClassService;
import com.nghiatr.ticket_booking.user.model.Organizer;
import com.nghiatr.ticket_booking.venue.entity.Venue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventFacadeStorageTest {

    @Mock
    private TicketClassService ticketClassService;

    @Mock
    private SeatService seatService;

    @Mock
    private EventService eventService;

    @Mock
    private EventActionValidator eventActionValidator;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileStorageValidator fileStorageValidator;

    @InjectMocks
    private EventFacade eventFacade;

    private UUID eventId;
    private Event existingEvent;
    private MockMultipartFile newImage;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();

        Organizer organizer = new Organizer();
        organizer.setUserId(UUID.randomUUID());

        Venue venue = new Venue();
        venue.setVenueId(UUID.randomUUID());

        existingEvent = Event.builder()
                .eventId(eventId)
                .eventName("Old Concert")
                .eventImgUrl("https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/old-img.jpg")
                .organizer(organizer)
                .venue(venue)
                .status(EventStatus.PENDING)
                .build();

        newImage = new MockMultipartFile(
                "image",
                "new-cover.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
    }

    @Test
    @DisplayName("update with new image uploads new image and updates entity")
    void testUpdateWithNewImageSuccess() {
        EventUpdateRequest updateRequest = new EventUpdateRequest(
                "Updated Concert",
                "New description",
                null,
                null,
                null,
                null,
                null
        );

        when(eventService.getOrganizerEvent(eventId)).thenReturn(existingEvent);
        when(ticketClassService.getAllByEvent(existingEvent)).thenReturn(Collections.emptyList());

        String newS3Url = "https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/new-img.png";
        when(fileStorageService.uploadFile(eq(newImage), eq("events"))).thenReturn(newS3Url);

        EventItemResponse response = eventFacade.update(eventId, updateRequest, newImage);

        verify(eventActionValidator).validate(existingEvent, Collections.emptyList(), updateRequest, EventActionType.UPDATE);
        verify(fileStorageValidator).validateImageFile(newImage);
        verify(fileStorageService).uploadFile(newImage, "events");
        verify(eventService).saveEvent(existingEvent);

        assertEquals(newS3Url, existingEvent.getEventImgUrl());
        assertEquals(newS3Url, response.eventImgUrl());
    }

    @Test
    @DisplayName("update when validation fails does NOT upload new image to S3")
    void testUpdateValidationFailsDoesNotUploadToS3() {
        EventUpdateRequest updateRequest = new EventUpdateRequest(
                "Locked Concert",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(eventService.getOrganizerEvent(eventId)).thenReturn(existingEvent);
        when(ticketClassService.getAllByEvent(existingEvent)).thenReturn(Collections.emptyList());

        doThrow(new AppException(EventErrorCode.EVENT_LOCKED))
                .when(eventActionValidator).validate(any(), any(), any(), any());

        AppException ex = assertThrows(AppException.class, () ->
                eventFacade.update(eventId, updateRequest, newImage)
        );

        assertEquals(EventErrorCode.EVENT_LOCKED, ex.getErrorCode());
        // Verify S3 upload is never called
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("update without image keeps existing image and does not call S3 upload")
    void testUpdateWithoutImageKeepsExisting() {
        EventUpdateRequest updateRequest = new EventUpdateRequest(
                "Updated Concert",
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(eventService.getOrganizerEvent(eventId)).thenReturn(existingEvent);
        when(ticketClassService.getAllByEvent(existingEvent)).thenReturn(Collections.emptyList());

        eventFacade.update(eventId, updateRequest, null);

        verifyNoInteractions(fileStorageService);
        assertEquals("https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/old-img.jpg", existingEvent.getEventImgUrl());
    }
}
