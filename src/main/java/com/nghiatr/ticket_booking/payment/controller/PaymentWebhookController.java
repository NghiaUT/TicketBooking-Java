package com.nghiatr.ticket_booking.payment.controller;

import com.nghiatr.ticket_booking.orchestration.PaymentFacade;
import com.nghiatr.ticket_booking.payment.dto.PaymentCallbackResult;
import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentFacade paymentFacade;

    /**
     * IPN Webhook Server-to-Server qua phương thức GET (Phổ biến như VNPay IPN).
     */
    @GetMapping("/{method}/ipn")
    public ResponseEntity<Map<String, String>> handleGetIpn(
            @PathVariable("method") PaymentMethod method,
            @RequestParam Map<String, String> allParams
    ) {
        log.info("[Webhook IPN GET] Nhận callback từ cổng {}: {}", method, allParams);
        PaymentCallbackResult result = paymentFacade.processIpnPayment(method, allParams);

        if (result.success()) {
            return ResponseEntity.ok(Map.of("RspCode", "00", "Message", "Confirm Success"));
        } else {
            return ResponseEntity.ok(Map.of("RspCode", result.responseCode() != null ? result.responseCode() : "99",
                    "Message", result.message()));
        }
    }

    /**
     * IPN Webhook Server-to-Server qua phương thức POST (Phổ biến như Momo, Stripe, ZaloPay).
     */
    @PostMapping("/{method}/ipn")
    public ResponseEntity<Map<String, String>> handlePostIpn(
            @PathVariable("method") PaymentMethod method,
            @RequestBody Map<String, String> bodyParams
    ) {
        log.info("[Webhook IPN POST] Nhận callback từ cổng {}: {}", method, bodyParams);
        PaymentCallbackResult result = paymentFacade.processIpnPayment(method, bodyParams);

        if (result.success()) {
            return ResponseEntity.ok(Map.of("status", "00", "message", "Success"));
        } else {
            return ResponseEntity.ok(Map.of("status", result.responseCode() != null ? result.responseCode() : "99",
                    "message", result.message()));
        }
    }
}
