package com.nghiatr.ticket_booking.shared.jobs.expired_seats;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReleaseExpiredSeatJob {
    private final ExpiredSeatService expiredSeatService;

    @Scheduled(cron = "*/30 * * * * *")
    public void startReleaseExpiredSeat() {
        System.out.println("[CronJob] Checking expired seats...");

        try {
            expiredSeatService.releaseExpiredSeats();
        } catch (Exception e) {
            System.out.println("[CronJob] releaseExpiredSeats error" + e);
        }
    }
}
