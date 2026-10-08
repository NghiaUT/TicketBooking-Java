package com.nghiatr.ticket_booking.event.service;

import com.nghiatr.ticket_booking.event.dto.CreateEventRequest;
import com.nghiatr.ticket_booking.event.dto.EventItemResponse;
import com.nghiatr.ticket_booking.event.dto.EventResponse;
import com.nghiatr.ticket_booking.event.dto.EventUpdateRequest;
import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.entity.EventStatus;
import com.nghiatr.ticket_booking.event.exception.EventErrorCode;
import com.nghiatr.ticket_booking.event.repository.EventRepository;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.shared.utils.SecurityUtils;
import com.nghiatr.ticket_booking.user.model.Organizer;
import com.nghiatr.ticket_booking.user.repository.OrganizerRepository;
import com.nghiatr.ticket_booking.venue.entity.Venue;
import com.nghiatr.ticket_booking.venue.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.nghiatr.ticket_booking.shared.storage.FileStorageService;
import com.nghiatr.ticket_booking.shared.storage.FileStorageValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final VenueService venueService;
    private final SecurityUtils securityUtils;
    private final FileStorageService fileStorageService;
    private final FileStorageValidator fileStorageValidator;

    /**
     * Lấy thực thể sự kiện theo ID thuộc quyền sở hữu của ban tổ chức hiện tại.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @return thực thể Event tìm thấy
     * @throws AppException nếu không tìm thấy sự kiện hoặc không thuộc quyền sở hữu
     */
    public Event getOrganizerEvent(UUID eventId) {
        UUID organizerId = securityUtils.getCurrentUserId();

        return eventRepository
                .findByEventIdAndOrganizer_UserId(eventId, organizerId)
                .orElseThrow(() ->
                        new AppException(EventErrorCode.EVENT_NOT_FOUND)
                );
    }

    /**
     * Tìm kiếm thực thể sự kiện theo mã định danh sự kiện.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @return thực thể Event tìm thấy
     * @throws AppException nếu không tìm thấy sự kiện
     */
    public Event getEvent(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
    }

    /**
     * Lưu hoặc cập nhật thực thể sự kiện vào cơ sở dữ liệu.
     *
     * @param event thực thể sự kiện cần lưu
     * @return thực thể Event sau khi lưu
     */
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    /**
     * Lấy danh sách toàn bộ các sự kiện đang ở trạng thái đã duyệt (APPROVED).
     *
     * @return đối tượng EventResponse chứa danh sách sự kiện công khai
     */
    public EventResponse findAllEvent() {
        List<EventItemResponse> events = eventRepository
                .findAllByStatus(EventStatus.APPROVED)
                .stream()
                .map(EventItemResponse::from)
                .toList();

        return EventResponse.builder()
                .events(events)
                .build();
    }

    /**
     * Lấy thông tin chi tiết của sự kiện theo ID dưới dạng DTO.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @return đối tượng EventItemResponse chứa chi tiết sự kiện
     * @throws AppException nếu sự kiện không tồn tại
     */
    public EventItemResponse findEventById(UUID eventId) {
        return eventRepository.
                findById(eventId)
                .map(EventItemResponse::from)
                .orElseThrow(() -> new AppException(EventErrorCode.EVENT_NOT_FOUND));
    }

    /**
     * Lấy danh sách toàn bộ sự kiện do một ban tổ chức cụ thể tạo ra.
     *
     * @param organizerId định danh ban tổ chức
     * @return đối tượng EventResponse chứa danh sách sự kiện của ban tổ chức
     */
    public EventResponse findOrganizerEvent(UUID organizerId) {
        return EventResponse.builder()
                .events(
                eventRepository.findAllByOrganizer_UserId(organizerId)
                        .stream()
                        .map(EventItemResponse::from)
                        .toList()
                )
                .build();
    }

    /**
     * Tạo mới thông tin cơ bản của sự kiện mà không kèm tệp ảnh tải lên.
     *
     * @param organizerId định danh ban tổ chức
     * @param eventData dữ liệu thông tin cơ bản sự kiện
     * @return đối tượng EventItemResponse chứa thông tin sự kiện vừa tạo
     */
    @Transactional
    public EventItemResponse createBasicInfo(UUID organizerId, CreateEventRequest eventData) {
        return createBasicInfo(organizerId, eventData, null);
    }

    /**
     * Tạo mới thông tin cơ bản của sự kiện kèm xử lý lưu trữ tệp ảnh bìa.
     *
     * @param organizerId định danh ban tổ chức
     * @param eventData dữ liệu thông tin cơ bản sự kiện
     * @param image tệp ảnh bìa tải lên (tùy chọn)
     * @return đối tượng EventItemResponse chứa thông tin sự kiện vừa tạo
     * @throws AppException nếu thời gian tổ chức không hợp lệ hoặc không tìm thấy ban tổ chức/địa điểm
     */
    @Transactional
    public EventItemResponse createBasicInfo(UUID organizerId, CreateEventRequest eventData, MultipartFile image) {
        LocalDateTime timeToStart = eventData.getTimeToStart();
        LocalDateTime timeToRelease = eventData.getTimeToRelease();

        if (timeToStart.isBefore(timeToRelease)
                || timeToStart.isBefore(LocalDateTime.now())
                || timeToRelease.isBefore(LocalDateTime.now())
        ) {
            throw new AppException(EventErrorCode.INVALID_EVENT_TIME);
        }

        Venue venue = venueService.getVenueById(eventData.getVenueId());

        Organizer organizer = organizerRepository.findByUserId(organizerId)
                .orElseThrow(() -> new AppException(EventErrorCode.ORGANIZER_NOT_FOUND));

        // Validate image trước nếu client có đính kèm file
        boolean hasImage = image != null && !image.isEmpty();
        if (hasImage) {
            fileStorageValidator.validateImageFile(image);
        }

        String imageUrl = eventData.getEventImgUrl();
        if (hasImage) {
            // Upload ảnh lên S3
            imageUrl = fileStorageService.uploadFile(image, "events");

            // Đăng ký rollback compensation: Nếu transaction rollback, xóa ảnh trên S3
            final String uploadedImageUrl = imageUrl;
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                            fileStorageService.deleteFile(uploadedImageUrl);
                        }
                    }
                });
            }
        }

        try {
            Event newEvent = Event.builder()
                    .eventName(eventData.getEventName())
                    .venue(venue)
                    .organizer(organizer)
                    .eventImgUrl(imageUrl)
                    .dateToStart(eventData.getDateToStart())
                    .timeToRelease(eventData.getTimeToRelease())
                    .timeToStart(eventData.getTimeToStart())
                    .description(eventData.getDescription())
                    .genre(eventData.getGenre())
                    .duration(eventData.getDuration())
                    .status(EventStatus.PENDING)
                    .build();
            eventRepository.save(newEvent);

            return EventItemResponse.from(newEvent);
        } catch (Exception ex) {
            if (hasImage && imageUrl != null) {
                fileStorageService.deleteFile(imageUrl);
            }
            throw ex;
        }
    }

    // ======== Cập nhật các field khác của Event.
    /**
     * Cập nhật các trường thông tin thay đổi vào thực thể sự kiện hiện có.
     *
     * @param event thực thể sự kiện cần cập nhật
     * @param request dữ liệu cập nhật sự kiện
     */
    public void updateEventFields(
            Event event,
            EventUpdateRequest request
    ) {
        if (request.eventName() != null) {
            event.setEventName(request.eventName());
        }

        if (request.description() != null) {
            event.setDescription(request.description());
        }

        if (request.status() != null) {
            event.setStatus(request.status());
        }

        if (request.dateToStart() != null) {
            event.setDateToStart(request.dateToStart());
        }

        if (request.timeToStart() != null) {
            event.setTimeToStart(request.timeToStart());
        }
    }
}
