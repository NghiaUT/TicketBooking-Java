package com.nghiatr.ticket_booking.payment.strategy;

import com.nghiatr.ticket_booking.payment.dto.PaymentCallbackResult;
import com.nghiatr.ticket_booking.payment.dto.PaymentCommand;
import com.nghiatr.ticket_booking.payment.dto.PaymentInitResult;
import com.nghiatr.ticket_booking.payment.dto.PaymentReturnResult;
import com.nghiatr.ticket_booking.payment.entity.PaymentMethod;

import java.util.Map;

public interface PaymentStrategy {

    /**
     * Định danh phương thức thanh toán mà Strategy này xử lý.
     */
    PaymentMethod getPaymentMethod();

    /**
     * Khởi tạo giao dịch với cổng thanh toán và trả về URL/thông tin thanh toán.
     */
    PaymentInitResult initPayment(PaymentCommand command);

    /**
     * Xử lý IPN/Webhook từ server cổng thanh toán gửi sang (Server-to-Server).
     * Đây là nơi duy nhất để xác thực và cập nhật trạng thái thanh toán vào database.
     */
    PaymentCallbackResult handleIpnCallback(Map<String, String> params);

    /**
     * Phân tích tham số khi người dùng được trình duyệt redirect về Return URL.
     * Thao tác này chỉ dùng để hiển thị giao diện thông báo, không thực hiện ghi DB.
     */
    PaymentReturnResult parseReturnParams(Map<String, String> params);
}
