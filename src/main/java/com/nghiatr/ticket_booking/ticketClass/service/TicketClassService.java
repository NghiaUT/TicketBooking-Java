package com.nghiatr.ticket_booking.ticketClass.service;

import com.nghiatr.ticket_booking.event.entity.Event;
import com.nghiatr.ticket_booking.event.service.EventService;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassItem;
import com.nghiatr.ticket_booking.ticketClass.dto.TicketClassResponse;
import com.nghiatr.ticket_booking.ticketClass.entity.TicketClass;
import com.nghiatr.ticket_booking.ticketClass.exception.TicketClassErrorCode;
import com.nghiatr.ticket_booking.ticketClass.repository.TicketClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketClassService {
    private final TicketClassRepository ticketClassRepository;
    private final EventService eventService;

    private boolean isUUID(String id) {
        return id != null && id.matches(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
        );
    }

    /**
     * Tìm kiếm thực thể hạng vé theo mã định danh duy nhất.
     *
     * @param ticketClassId định danh duy nhất của hạng vé
     * @return thực thể TicketClass tìm thấy
     * @throws AppException nếu hạng vé không tồn tại
     */
    public TicketClass getById(UUID ticketClassId) {
        return ticketClassRepository.findById(ticketClassId)
                .orElseThrow(() -> new AppException(TicketClassErrorCode.TICKET_CLASS_NOT_FOUND));
    }

    /**
     * Lấy toàn bộ danh sách hạng vé thuộc về một sự kiện.
     *
     * @param event thực thể sự kiện cần lấy hạng vé
     * @return danh sách các thực thể TicketClass của sự kiện
     */
    public List<TicketClass> getAllByEvent(Event event) {
        return ticketClassRepository.findAllByEventId(event);
    }

    /**
     * Lưu danh sách các thực thể hạng vé vào cơ sở dữ liệu.
     *
     * @param ticketClasses danh sách các thực thể hạng vé cần lưu
     * @return danh sách các thực thể TicketClass sau khi lưu
     */
    public List<TicketClass> saveAll(List<TicketClass> ticketClasses) {
        return ticketClassRepository.saveAll(ticketClasses);
    }

    /**
     * Tạo mới danh sách các hạng vé cho một sự kiện cụ thể.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @param ticketClassItemData danh sách thông tin các hạng vé cần tạo
     * @return số lượng hạng vé đã được lưu vào cơ sở dữ liệu
     */
    public int createTicketClasses(UUID eventId, List<TicketClassItem> ticketClassItemData) {

        Event event = eventService.getEvent(eventId);

        List<TicketClass> ticketClasses = ticketClassItemData.stream()
                .map(ticket -> TicketClass.builder()
                        .eventId(event)
                        .className(ticket.getClassName())
                        .price(ticket.getPrice())
                        .quota(ticket.getQuota())
                        .type(ticket.getType())
                        .color(ticket.getColor())
                        .description(ticket.getDescription())
                        .build())
                .toList();

        return ticketClassRepository.saveAll(ticketClasses).size();
    }

    /**
     * Cập nhật danh sách hạng vé của sự kiện (thêm mới, cập nhật và xóa các hạng vé bị gỡ bỏ).
     *
     * @param eventId định danh duy nhất của sự kiện
     * @param ticketClassItemData danh sách thông tin hạng vé gửi lên từ client
     * @return số lượng hạng vé hiện có của sự kiện sau khi cập nhật
     * @throws AppException nếu không tìm thấy sự kiện hoặc hạng vé cần sửa
     */
    @Transactional
    public int editTicketClasses(UUID eventId, List<TicketClassItem> ticketClassItemData) {
        // 1. Xóa những ticket class bị xóa trên frontend:

        Event event = eventService.getEvent(eventId);

        List<TicketClass> existingTickets =
                ticketClassRepository.findAllByEventId(event);

        // 2. Lấy ID các ticket class frontend vẫn còn giữ
        Set<String> incomingIds = ticketClassItemData.stream()
                .map(TicketClassItem::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. Những ID DB không xuất hiện trong request => đã bị xóa
        List<TicketClass> deletedTickets = existingTickets.stream()
                .filter(ticket -> !incomingIds.contains(ticket.getTicketClassId().toString()))
                .toList();

        ticketClassRepository.deleteAll(deletedTickets);

        List<TicketClass> ticketClasses = ticketClassItemData.stream()
                .map(ticket -> {
                    if(!isUUID(ticket.getId())) {
                        // Tạo mới:
                        return TicketClass.builder()
                                .eventId(event)
                                .className(ticket.getClassName())
                                .price(ticket.getPrice())
                                .quota(ticket.getQuota())
                                .type(ticket.getType())
                                .color(ticket.getColor())
                                .description(ticket.getDescription())
                                .build();
                    }

                    // Vé cũ
                    TicketClass ticketClass = ticketClassRepository.findById(UUID.fromString(ticket.getId()))
                            .orElseThrow(() -> new AppException(TicketClassErrorCode.TICKET_CLASS_NOT_FOUND));

                    ticketClass.setClassName(ticket.getClassName());
                    ticketClass.setPrice(ticket.getPrice());
                    ticketClass.setQuota(ticket.getQuota());
                    ticketClass.setType(ticket.getType());
                    ticketClass.setColor(ticket.getColor());
                    ticketClass.setDescription(ticket.getDescription());

                    return ticketClass;
                })
                .toList();

        return ticketClassRepository.saveAll(ticketClasses).size();
    }

    /**
     * Lấy danh sách hạng vé của một sự kiện theo ID sự kiện.
     *
     * @param eventId định danh duy nhất của sự kiện
     * @return danh sách các đối tượng TicketClassResponse
     */
    public List<TicketClassResponse> getByEventId(UUID eventId) {

        Event event = eventService.getEvent(eventId);

        List<TicketClass> ticketClasses = ticketClassRepository.findAllByEventId(event);

        return ticketClasses.stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketClassResponse toResponse(TicketClass ticketClass) {
        return new TicketClassResponse(
                ticketClass.getTicketClassId(),
                ticketClass.getEventId().getEventId(),
                ticketClass.getClassName(),
                ticketClass.getPrice(),
                ticketClass.getQuota(),
                ticketClass.getType(),
                ticketClass.getColor(),
                ticketClass.getDescription()
        );
    }
}
