package com.nghiatr.ticket_booking.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nghiatr.ticket_booking.event.dto.CreateEventRequest;
import com.nghiatr.ticket_booking.event.dto.EventItemResponse;
import com.nghiatr.ticket_booking.event.dto.EventUpdateRequest;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.event.service.EventService;
import com.nghiatr.ticket_booking.orchestration.EventFacade;
import com.nghiatr.ticket_booking.shared.handler.GlobalExceptionHandler;
import com.nghiatr.ticket_booking.ticketClass.service.TicketClassService;
import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerMultipartTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @Mock
    private TicketClassService ticketClassService;

    @Mock
    private EventFacade eventFacade;

    @Mock
    private CustomUserDetails userDetails;

    @InjectMocks
    private EventController eventController;

    private ObjectMapper objectMapper;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        org.mockito.Mockito.lenient().when(userDetails.getUserId()).thenReturn(userId);

        HandlerMethodArgumentResolver authPrincipalResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return userDetails;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
                .setCustomArgumentResolvers(authPrincipalResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /my-event with multipart/form-data should call createBasicInfo with image")
    void testCreateBasicInfoEventMultipart() throws Exception {
        UUID venueId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "eventName": "Rock Festival",
                    "venueId": "%s",
                    "genre": "Rock",
                    "description": "Annual rock festival",
                    "timeToStart": "2026-10-10T10:00:00",
                    "dateToStart": "2026-10-10T10:00:00",
                    "timeToRelease": "2026-09-20T10:00:00",
                    "duration": "180"
                }
                """.formatted(venueId);

        MockMultipartFile eventPart = new MockMultipartFile(
                "event",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonPayload.getBytes()
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "rock.png",
                "image/png",
                new byte[]{1, 2, 3, 4}
        );

        UUID eventId = UUID.randomUUID();
        String imageUrl = "https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/uuid.png";
        EventItemResponse responseDto = new EventItemResponse(
                eventId,
                "Rock Festival",
                userId,
                venueId,
                "Rock",
                imageUrl,
                "Annual rock festival",
                EventStatus.PENDING,
                LocalDateTime.parse("2026-10-10T10:00:00"),
                LocalDateTime.parse("2026-10-10T10:00:00"),
                LocalDateTime.parse("2026-09-20T10:00:00"),
                "180",
                null,
                null
        );

        when(eventService.createBasicInfo(eq(userId), any(CreateEventRequest.class), any()))
                .thenReturn(responseDto);

        mockMvc.perform(multipart("/api/v1/events/my-event")
                        .file(eventPart)
                        .file(imagePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.data.eventImageUrl").value(imageUrl))
                .andExpect(jsonPath("$.data.eventImgUrl").value(imageUrl));

        verify(eventService).createBasicInfo(eq(userId), any(CreateEventRequest.class), any());
    }

    @Test
    @DisplayName("POST /my-event with application/json should call createBasicInfo with null image")
    void testCreateBasicInfoEventJson() throws Exception {
        UUID venueId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "eventName": "Jazz Night",
                    "venueId": "%s",
                    "genre": "Jazz",
                    "description": "Smooth jazz",
                    "timeToStart": "2026-10-10T10:00:00"
                }
                """.formatted(venueId);

        UUID eventId = UUID.randomUUID();
        EventItemResponse responseDto = new EventItemResponse(
                eventId,
                "Jazz Night",
                userId,
                venueId,
                "Jazz",
                null,
                "Smooth jazz",
                EventStatus.PENDING,
                LocalDateTime.parse("2026-10-10T10:00:00"),
                null,
                null,
                null,
                null,
                null
        );

        when(eventService.createBasicInfo(eq(userId), any(CreateEventRequest.class), eq(null)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/events/my-event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").value(eventId.toString()))
                .andExpect(jsonPath("$.data.eventName").value("Jazz Night"));

        verify(eventService).createBasicInfo(eq(userId), any(CreateEventRequest.class), eq(null));
    }

    @Test
    @DisplayName("PUT /my-event/{id} with multipart/form-data should call facade update with image")
    void testUpdateEventInfoMultipart() throws Exception {
        UUID eventId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "eventName": "Updated Jazz Night",
                    "description": "New description"
                }
                """;

        MockMultipartFile eventPart = new MockMultipartFile(
                "event",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                jsonPayload.getBytes()
        );

        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "new-cover.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3}
        );

        String updatedImageUrl = "https://test-bucket.s3.ap-southeast-1.amazonaws.com/events/new.jpg";
        EventItemResponse responseDto = new EventItemResponse(
                eventId,
                "Updated Jazz Night",
                userId,
                UUID.randomUUID(),
                "Jazz",
                updatedImageUrl,
                "New description",
                EventStatus.PENDING,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(eventFacade.update(eq(eventId), any(EventUpdateRequest.class), any()))
                .thenReturn(responseDto);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/events/my-event/" + eventId)
                        .file(eventPart)
                        .file(imagePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventImageUrl").value(updatedImageUrl));

        verify(eventFacade).update(eq(eventId), any(EventUpdateRequest.class), any());
    }

    @Test
    @DisplayName("PUT /my-event/{id} with application/json should call facade update with null image")
    void testUpdateEventInfoJson() throws Exception {
        UUID eventId = UUID.randomUUID();
        String jsonPayload = """
                {
                    "eventName": "Updated Event",
                    "description": "Updated Description"
                }
                """;

        EventItemResponse responseDto = new EventItemResponse(
                eventId,
                "Updated Event",
                userId,
                UUID.randomUUID(),
                "Genre",
                null,
                "Updated Description",
                EventStatus.PENDING,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(eventFacade.update(eq(eventId), any(EventUpdateRequest.class), eq(null)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/api/v1/events/my-event/" + eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventName").value("Updated Event"));

        verify(eventFacade).update(eq(eventId), any(EventUpdateRequest.class), eq(null));
    }
}
