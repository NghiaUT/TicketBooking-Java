package com.nghiatr.ticket_booking.venue.controller;

import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import com.nghiatr.ticket_booking.venue.dto.VenueResponse;
import com.nghiatr.ticket_booking.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {
    private final VenueService venueService;

    // Public.
    /**
     * Lấy danh sách tất cả các địa điểm tổ chức sự kiện công khai trong hệ thống.
     *
     * @return phản hồi HTTP chứa ApiResponse với VenueResponse gồm danh sách địa điểm
     */
    @GetMapping
    public ResponseEntity<ApiResponse<VenueResponse>> findAllVenuesPublic() {
        return ResponseEntity.ok(
                ApiResponse.ok(
                    VenueResponse.builder()
                            .venues(venueService.findAllVenues())
                            .build(),
                        "Lấy danh sách địa điểm thành công"
                )
        );
    }
}
