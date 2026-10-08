package com.nghiatr.ticket_booking.payment.strategy;

import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;
import com.nghiatr.ticket_booking.payment.exception.PaymentErrorCode;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    /**
     * Khởi tạo factory và thu thập tất cả các bean cài đặt PaymentStrategy vào map.
     *
     * @param strategyList danh sách các chiến lược thanh toán được Spring tự động nạp
     */
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getPaymentMethod, Function.identity()));
    }

    /**
     * Lấy chiến lược xử lý thanh toán tương ứng với phương thức được chọn.
     *
     * @param method phương thức thanh toán
     * @return đối tượng PaymentStrategy tương ứng
     * @throws AppException nếu phương thức thanh toán chưa được hệ thống hỗ trợ
     */
    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new AppException(
                    PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD,
                    "Chưa hỗ trợ phương thức thanh toán: " + method
            );
        }
        return strategy;
    }
}
