package com.nghiatr.ticket_booking.event.service;

import com.nghiatr.ticket_booking.event.dto.CreateEventRequest;
import com.nghiatr.ticket_booking.event.dto.EventItemResponse;
import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.event.repository.EventRepository;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.shared.storage.FileStorageService;
import com.nghiatr.ticket_booking.shared.storage.FileStorageValidator;
import com.nghiatr.ticket_booking.shared.utils.SecurityUtils;
import com.nghiatr.ticket_booking.user.model.Organizer;
import com.nghiatr.ticket_booking.user.repository.OrganizerRepository;
import com.nghiatr.ticket_booking.venue.entity.Venue;
import com.nghiatr.ticket_booking.venue.service.VenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceStorageTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private VenueService venueService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileStorageValidator fileStorageValidator;

    @InjectMocks
    private EventService eventService;

    private UUID organizerId;
    private UUID venueId;
    private CreateEventRequest request;
    private MockMultipartFile imageFile;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        venueId = UUID.randomUUID();

        request = new CreateEventRequest();
        request.setEventName("Concert 2026");
        request.setVenueId(venueId);
        request.setGenre("Rock");
        request.setDescription("Live concert");
        request.setTimeToStart(LocalDateTime.now().plusDays(10));
        request.setDateToStart(LocalDateTime.now().plusDays(10));
        request.setTimeToRelease(LocalDateTime.now().plusDays(1));
        request.setDuration("120");

        imageFile = new MockMultipartFile(
                "image",
                "cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );
    }

    @Test
    @DisplayName("createBasicInfo with image uploads to S3 and saves eventImageUrl")
    void testCreateBasicInfoWithImageSuccess() {
        Venue venue = new Venue();
        venue.setVenueId(venueId);

        Organizer organizer = new Organizer();
        organizer.setUserId(organizerId);

        when(venueService.getVenueById(venueId)).thenReturn(venue);
        when(organizerRepository.findByUserId(organizerId)).thenReturn(Optional.of(organizer));

        String expectedS3Url = "https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/uuid.jpg";
        when(fileStorageService.uploadFile(eq(imageFile), eq("events"))).thenReturn(expectedS3Url);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setEventId(UUID.randomUUID());
            return event;
        });

        EventItemResponse response = eventService.createBasicInfo(organizerId, request, imageFile);

        verify(fileStorageValidator).validateImageFile(imageFile);
        verify(fileStorageService).uploadFile(imageFile, "events");

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());

        Event savedEvent = eventCaptor.getValue();
        assertEquals("Concert 2026", savedEvent.getEventName());
        assertEquals(expectedS3Url, savedEvent.getEventImgUrl());
        assertEquals(expectedS3Url, savedEvent.getEventImgUrl());
        assertEquals(expectedS3Url, response.eventImgUrl());
    }

    @Test
    @DisplayName("createBasicInfo with invalid timeline fails before S3 upload is ever called")
    void testCreateBasicInfoInvalidTimelineDoesNotUploadToS3() {
        // timeToStart before timeToRelease
        request.setTimeToStart(LocalDateTime.now().plusDays(1));
        request.setTimeToRelease(LocalDateTime.now().plusDays(5));

        AppException ex = assertThrows(AppException.class, () ->
                eventService.createBasicInfo(organizerId, request, imageFile)
        );

        assertEquals(EventErrorCode.INVALID_EVENT_TIME, ex.getErrorCode());
        // Verify S3 was never called
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("createBasicInfo when venue not found fails before S3 upload is ever called")
    void testCreateBasicInfoVenueNotFoundDoesNotUploadToS3() {
        when(venueService.getVenueById(venueId)).thenThrow(new AppException(EventErrorCode.VENUE_NOT_FOUND));

        assertThrows(AppException.class, () ->
                eventService.createBasicInfo(organizerId, request, imageFile)
        );

        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("createBasicInfo when DB save fails triggers compensating S3 delete")
    void testCreateBasicInfoDbSaveFailureCompensatesS3() {
        Venue venue = new Venue();
        venue.setVenueId(venueId);

        Organizer organizer = new Organizer();
        organizer.setUserId(organizerId);

        when(venueService.getVenueById(venueId)).thenReturn(venue);
        when(organizerRepository.findByUserId(organizerId)).thenReturn(Optional.of(organizer));

        String uploadedUrl = "https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/uuid.jpg";
        when(fileStorageService.uploadFile(eq(imageFile), eq("events"))).thenReturn(uploadedUrl);

        // Database write throws runtime exception
        when(eventRepository.save(any(Event.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () ->
                eventService.createBasicInfo(organizerId, request, imageFile)
        );

        // Verify newly uploaded file on S3 is deleted to prevent orphaned files
        verify(fileStorageService).deleteFile(uploadedUrl);
    }
}
