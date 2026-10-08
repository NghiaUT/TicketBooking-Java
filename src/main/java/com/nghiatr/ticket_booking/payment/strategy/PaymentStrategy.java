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
     *
     * @return phương thức thanh toán PaymentMethod tương ứng
     */
    PaymentMethod getPaymentMethod();

    /**
     * Khởi tạo giao dịch với cổng thanh toán và trả về URL/thông tin thanh toán.
     *
     * @param command dữ liệu lệnh khởi tạo thanh toán
     * @return kết quả khởi tạo thanh toán chứa URL chuyển hướng
     */
    PaymentInitResult initPayment(PaymentCommand command);

    /**
     * Xử lý IPN/Webhook từ server cổng thanh toán gửi sang (Server-to-Server).
     *
     * @param params tập các tham số do cổng thanh toán gửi qua webhook
     * @return kết quả xử lý callback từ cổng thanh toán
     */
    PaymentCallbackResult handleIpnCallback(Map<String, String> params);

    /**
     * Phân tích tham số khi người dùng được trình duyệt chuyển hướng về URL trả về.
     *
     * @param params tập các tham số query params nhận từ URL redirect
     * @return kết quả phân tích phục vụ hiển thị trên giao diện người dùng
     */
    PaymentReturnResult parseReturnParams(Map<String, String> params);
}
