package com.nghiatr.ticket_booking.payment.strategy;

import com.nghiatr.ticket_booking.payment.dto.PaymentCallbackResult;
import com.nghiatr.ticket_booking.payment.dto.PaymentCommand;
import com.nghiatr.ticket_booking.payment.dto.PaymentInitResult;
import com.nghiatr.ticket_booking.payment.dto.PaymentReturnResult;
import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class MockPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.MOCK;
    }

    @Override
    public PaymentInitResult initPayment(PaymentCommand command) {
        // Sinh link giả lập thanh toán cho môi trường Dev / Testing
        String mockPaymentUrl = String.format(
                "http://localhost:8080/api/v1/payments/mock/checkout?code=%s&amount=%.2f",
                command.transactionCode(),
                command.amount()
        );

        return PaymentInitResult.builder()
                .paymentId(command.paymentId())
                .paymentUrl(mockPaymentUrl)
                .method(PaymentMethod.MOCK)
                .transactionCode(command.transactionCode())
                .metadata(Map.of("simulation", true))
                .build();
    }

    @Override
    public PaymentCallbackResult handleIpnCallback(Map<String, String> params) {
        String transactionCode = params.get("transactionCode");
        String status = params.getOrDefault("status", "SUCCESS");
        boolean isSuccess = "SUCCESS".equalsIgnoreCase(status);

        return PaymentCallbackResult.builder()
                .success(isSuccess)
                .transactionCode(transactionCode)
                .gatewayTransactionId("MOCK-" + UUID.randomUUID())
                .amount(params.containsKey("amount") ? Double.parseDouble(params.get("amount")) : 0.0)
                .responseCode(isSuccess ? "00" : "99")
                .message(isSuccess ? "Thanh toán giả lập thành công" : "Thanh toán giả lập thất bại")
                .rawData(params)
                .build();
    }

    @Override
    public PaymentReturnResult parseReturnParams(Map<String, String> params) {
        String transactionCode = params.get("transactionCode");
        String status = params.getOrDefault("status", "SUCCESS");
        boolean isSuccess = "SUCCESS".equalsIgnoreCase(status);

        return PaymentReturnResult.builder()
                .success(isSuccess)
                .transactionCode(transactionCode)
                .message(isSuccess ? "Giao dịch thành công (MOCK)" : "Giao dịch thất bại (MOCK)")
                .build();
    }
}
