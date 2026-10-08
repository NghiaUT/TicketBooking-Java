package com.nghiatr.ticket_booking.venue.service;

import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.venue.entity.Venue;
import com.nghiatr.ticket_booking.venue.exception.VenueErrorCode;
import com.nghiatr.ticket_booking.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VenueService {
    private final VenueRepository venueRepository;

    /**
     * Tìm kiếm thực thể địa điểm theo mã định danh duy nhất.
     *
     * @param venueId định danh duy nhất của địa điểm
     * @return thực thể Venue tìm thấy
     * @throws AppException nếu địa điểm không tồn tại
     */
    public Venue getVenueById(UUID venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new AppException(VenueErrorCode.VENUE_NOT_FOUND));
    }

    /**
     * Lưu hoặc cập nhật thông tin địa điểm vào cơ sở dữ liệu.
     *
     * @param venue thực thể địa điểm cần lưu
     * @return thực thể Venue sau khi lưu
     */
    public Venue saveVenue(Venue venue) {
        return venueRepository.save(venue);
    }

    /**
     * Lấy danh sách toàn bộ các địa điểm tổ chức có trong cơ sở dữ liệu.
     *
     * @return danh sách các thực thể Venue
     */
    public List<Venue> findAllVenues() {
        return venueRepository.findAll();
    }
}
