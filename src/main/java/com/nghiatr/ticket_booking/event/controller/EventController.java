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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Events", description = "Quản lý sự kiện, sơ đồ ghế và các hạng vé")
@RestController
@RequestMapping ("/api/v1/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final TicketClassService ticketClassService;
    private final EventFacade eventFacade;

    /**
     * Lấy danh sách toàn bộ các sự kiện đã được phê duyệt công khai cho khách hàng.
     *
     * @return phản hồi HTTP chứa danh sách tóm tắt các sự kiện
     */
    @Operation(summary = "Lấy danh sách sự kiện công khai", description = "Lấy danh sách toàn bộ các sự kiện đã được phê duyệt công khai cho khách hàng")
    @GetMapping
    public ResponseEntity<ApiResponse<EventResponse>> findAllEventPublic() {
        // Thêm các cơ chế filter sau.
        return ResponseEntity.ok(ApiResponse.ok(eventService.findAllEvent(), "Lấy danh sách sự kiện thành công"));
    }

    /**
     * Lấy thông tin chi tiết của một sự kiện theo mã định danh.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @return phản hồi HTTP chứa thông tin chi tiết sự kiện
     */
    @Operation(summary = "Lấy chi tiết sự kiện", description = "Lấy thông tin chi tiết của một sự kiện theo mã định danh (UUID)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventItemResponse>> findEventById(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable("id") UUID eventId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(eventService.findEventById(eventId), "Lấy thông tin chi tiết sự kiện thành công"));
    }

    // ======= ORGANIZER
    /**
     * Lấy danh sách các sự kiện do ban tổ chức đang đăng nhập quản lý.
     *
     * @param userDetails thông tin người dùng ban tổ chức hiện tại
     * @return phản hồi HTTP chứa danh sách sự kiện của ban tổ chức
     */
    @Operation(summary = "Lấy danh sách sự kiện của ban tổ chức", description = "Lấy danh sách các sự kiện do ban tổ chức đang đăng nhập quản lý", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/my-event")
    public ResponseEntity<ApiResponse<EventResponse>> findOrganizerEvents(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
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
    /**
     * Tạo thông tin cơ bản cho sự kiện mới kèm tệp ảnh bìa (multipart/form-data).
     *
     * @param eventData dữ liệu thông tin cơ bản của sự kiện
     * @param image tệp hình ảnh bìa của sự kiện (tùy chọn)
     * @param userDetails thông tin ban tổ chức thực hiện tạo sự kiện
     * @return phản hồi HTTP chứa thông tin sự kiện vừa tạo
     */
    @Operation(summary = "Tạo sự kiện mới kèm ảnh bìa (Multipart)", description = "Tạo thông tin cơ bản cho sự kiện mới kèm tệp ảnh bìa (multipart/form-data)", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping(value = "/my-event", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> createBasicInfoEventMultipart(
            @Validated @RequestPart("event") CreateEventRequest eventData,
            @RequestPart(value = "image", required = false) org.springframework.web.multipart.MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
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
    /**
     * Tạo thông tin cơ bản cho sự kiện mới không kèm tệp ảnh tải lên (application/json).
     *
     * @param eventData dữ liệu thông tin cơ bản của sự kiện
     * @param userDetails thông tin ban tổ chức thực hiện tạo sự kiện
     * @return phản hồi HTTP chứa thông tin sự kiện vừa tạo
     */
    @Operation(summary = "Tạo sự kiện mới (JSON)", description = "Tạo thông tin cơ bản cho sự kiện mới không kèm tệp ảnh tải lên (application/json)", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping(value = "/my-event", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> createBasicInfoEvent(
            @Validated @RequestBody CreateEventRequest eventData,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
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
    /**
     * Tạo mới danh sách các hạng vé cho sự kiện.
     *
     * @param id định danh duy nhất của sự kiện
     * @param ticketClassRequest danh sách các hạng vé cần tạo
     * @return phản hồi HTTP chứa số lượng hạng vé đã tạo thành công
     */
    @Operation(summary = "Tạo các hạng vé cho sự kiện", description = "Tạo mới danh sách các hạng vé cho sự kiện", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/my-event/{id}/ticket-classes")
    public ResponseEntity<ApiResponse<Integer>> createTicketClasses(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable UUID id,
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

    /**
     * Chỉnh sửa danh sách các hạng vé của sự kiện.
     *
     * @param id định danh duy nhất của sự kiện
     * @param ticketClassRequest thông tin các hạng vé cần cập nhật
     * @return phản hồi HTTP chứa số lượng hạng vé đã chỉnh sửa thành công
     */
    @Operation(summary = "Chỉnh sửa các hạng vé", description = "Chỉnh sửa danh sách các hạng vé của sự kiện", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping("/my-event/{id}/ticket-classes")
    public ResponseEntity<ApiResponse<Integer>> editTicketClasses(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable UUID id,
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

    /**
     * Lấy danh sách tất cả các hạng vé của sự kiện.
     *
     * @param id định danh duy nhất của sự kiện
     * @return phản hồi HTTP chứa danh sách hạng vé
     */
    @Operation(summary = "Lấy danh sách hạng vé của sự kiện", description = "Lấy danh sách tất cả các hạng vé của sự kiện", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/my-event/{id}/ticket-classes")
    public ResponseEntity<ApiResponse<List<TicketClassResponse>>> getTicketClassesByEvent(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable UUID id
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
    /**
     * Khởi tạo sơ đồ vị trí ghế ngồi cho sự kiện.
     *
     * @param id định danh duy nhất của sự kiện
     * @param request dữ liệu thiết lập sơ đồ ghế
     * @return phản hồi HTTP chứa thông tin sơ đồ ghế đã tạo
     */
    @Operation(summary = "Tạo sơ đồ ghế cho sự kiện", description = "Khởi tạo sơ đồ vị trí ghế ngồi cho sự kiện", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/my-event/{id}/layout")
    public ResponseEntity<ApiResponse<SeatLayoutResponse>> createLayout(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable UUID id,
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
    /**
     * Cập nhật thông tin sự kiện kèm tải lên ảnh bìa mới (multipart/form-data).
     *
     * @param id định danh duy nhất của sự kiện
     * @param request dữ liệu các trường cần cập nhật
     * @param image tệp ảnh bìa mới (tùy chọn)
     * @return phản hồi HTTP chứa thông tin sự kiện sau khi cập nhật
     */
    @Operation(summary = "Cập nhật sự kiện kèm ảnh bìa (Multipart)", description = "Cập nhật thông tin sự kiện kèm tải lên ảnh bìa mới (multipart/form-data)", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping(value = "/my-event/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> updateEventInfoMultipart(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable UUID id,
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
    /**
     * Cập nhật thông tin sự kiện dưới dạng JSON (application/json).
     *
     * @param id định danh duy nhất của sự kiện
     * @param request dữ liệu các trường cần cập nhật
     * @return phản hồi HTTP chứa thông tin sự kiện sau khi cập nhật
     */
    @Operation(summary = "Cập nhật sự kiện (JSON)", description = "Cập nhật thông tin sự kiện dưới dạng JSON (application/json)", security = @SecurityRequirement(name = "BearerAuth"))
    @PutMapping(value = "/my-event/{id}", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EventItemResponse>> updateEventInfo(
            @Parameter(description = "ID duy nhất của sự kiện (UUID)", required = true) @PathVariable UUID id,
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
