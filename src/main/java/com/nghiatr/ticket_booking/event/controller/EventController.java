package com.nghiatr.ticket_booking.event.controller;

import com.nghiatr.ticket_booking.event.dto.*;
import com.nghiatr.ticket_booking.orchestration.EventFacade;
import com.nghiatr.ticket_booking.seat.dto.CreateLayoutRequest;
import com.nghiatr.ticket_booking.seat.dto.SeatLayoutResponse;
import com.nghiatr.ticket_booking.event.service.EventService;
import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassRequest;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassResponse;
import com.nghiatr.ticket_booking.ticketClass.service.TicketClassService;
import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping ("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final TicketClassService ticketClassService;
    private final EventFacade eventFacade;

    // Tìm kiếm tất cả sự kiện (API public dành cho khách hàng).
    @GetMapping
    public ResponseEntity<ApiResponse<EventResponse>> findAllEventPublic() {
        // Thêm các cơ chế filter sau.
        return ResponseEntity.ok(ApiResponse.ok(eventService.findAllEvent(), "Lấy danh sách sự kiện thành công"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventItemResponse>> findEventById(
            @PathVariable("id") UUID eventId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.findEventById(eventId), "Lấy thông tin chi tiết sự kiện thành công"));
    }

    // ======= ORGANIZER
    @GetMapping("/my-event")
    public ResponseEntity<ApiResponse<EventResponse>> findOrganizerEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        UUID organizerId = userDetails.getUserId();

        return ResponseEntity.ok(
                ApiResponse.ok(
                        eventService.findOrganizerEvent(organizerId),
                        "Lấy danh sách sự kiện thành công"
                )
        );
    }

    // [Bước 1] Tạo thông tin cơ bản kèm upload ảnh bìa (multipart/form-data)
    @PostMapping(value = "/my-event", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> createBasicInfoEventMultipart(
            @Validated @RequestPart("event") CreateEventRequest eventData,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID organizerId = userDetails.getUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(
                        eventService.createBasicInfo(organizerId, eventData, image),
                        "Tạo thông tin cơ bản sự kiện thành công."
                )
        );
    }

    // [Bước 1] Tạo thông tin cơ bản (application/json)
    @PostMapping(value = "/my-event", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> createBasicInfoEvent(
            @Validated @RequestBody CreateEventRequest eventData,
            @AuthenticationPrincipal CustomUserDetails userDetails
            ) {
        UUID organizerId = userDetails.getUserId();
        return ResponseEntity.ok(
                ApiResponse.ok(
                        eventService.createBasicInfo(organizerId, eventData, null),
                        "Tạo thông tin cơ bản sự kiện thành công."
                )
        );
    }

    //[Bước 2] Tạo các hạng vé:
    @PostMapping("/my-event/{id}/ticket-classes")
    public ResponseEntity<ApiResponse<Integer>> createTicketClasses(
            @PathVariable UUID id,
            @Validated @RequestBody TicketClassRequest ticketClassRequest
            ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        ticketClassService.createTicketClasses(
                                id,
                                ticketClassRequest.ticketClasses()
                        ),
                        "Tạo các hạng vé thành công!"
                )
        );
    }

    @PutMapping("/my-event/{id}/ticket-classes")
    public ResponseEntity<ApiResponse<Integer>> editTicketClasses(
            @PathVariable UUID id,
            @Validated @RequestBody TicketClassRequest ticketClassRequest
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        ticketClassService.editTicketClasses(
                                id,
                                ticketClassRequest.ticketClasses()
                        ),
                        "Chỉnh sửa các hạng vé thành công!"
                )
        );
    }

    @GetMapping("/my-event/{id}/ticket-classes")
    public ResponseEntity<ApiResponse<List<TicketClassResponse>>> getTicketClassesByEvent(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        ticketClassService.getByEventId(
                                id
                        ),
                        "Lấy danh sách thông tin hạng vé thành công!"
                )
        );
    }

    //[Bước 3] Tạo layout cho sự kiện
    @PostMapping("/my-event/{id}/layout")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> createLayout(
            @PathVariable UUID id,
            @RequestBody CreateLayoutRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        eventFacade.createLayout(
                                id,
                                request
                        ),
                "Tạo sơ đồ ghế thành công!"
                )
        );
    }

    // Chỉnh sửa các thông tin cho sự kiện kèm upload ảnh bìa (multipart/form-data)
    @PutMapping(value = "/my-event/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> updateEventInfoMultipart(
            @PathVariable UUID id,
            @RequestPart(value = "event", required = false) EventUpdateRequest request,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        eventFacade.update(
                                id,
                                request,
                                image
                        ),
                        "Cập nhật thông tin sự kiện thành công!"
                )
        );
    }

    // Chỉnh sửa các thông tin cho sự kiện (application/json)
    @PutMapping(value = "/my-event/{id}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> updateEventInfo(
            @PathVariable UUID id,
            @RequestBody EventUpdateRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(
                        eventFacade.update(
                                id,
                                request,
                                null
                        ),
                "Cập nhật thông tin sự kiện thành công!"
                )
        );
    }

}
