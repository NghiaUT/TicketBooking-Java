package com.nghiatr.ticket_booking.event.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping ("/api/v1/event")
public class EventController {

    @GetMapping
    public String getHome() {
        return "This is event page and protected elements, if you have accessed this it's mean you have successfully login";
    }
}
