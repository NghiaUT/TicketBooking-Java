package com.nghiatr.ticket_booking.seat.dto;

import com.nghiatr.ticket_booking.seat.entity.SeatLayout;

import java.util.List;

public record CreateLayoutRequest(
        CanvasRequest canvas,
        List<SeatBlockRequest> seatLayout
) {
    /**
     * Chuyển đổi dữ liệu yêu cầu tạo sơ đồ ghế thành đối tượng thực thể SeatLayout.
     *
     * @return đối tượng thực thể SeatLayout
     */
    public SeatLayout toSeatLayout() {
        return SeatLayout.builder()
                .seatLayout(
                        seatLayout.stream()
                                .map(SeatBlockRequest::toSeatBlock)
                                .toList()
                )
                .canvas(canvas.toCanvas())
                .build();
    }
}
