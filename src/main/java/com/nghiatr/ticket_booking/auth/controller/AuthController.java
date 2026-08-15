package com.nghiatr.ticket_booking.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @PostMapping("/login")
    public String login(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }

    @PostMapping("register")
    public String register(@RequestBody String entity) {
        //TODO: process POST request
        
        return entity;
    }
    
}
