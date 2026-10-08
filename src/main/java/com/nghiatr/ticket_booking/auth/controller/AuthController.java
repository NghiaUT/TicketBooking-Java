package com.nghiatr.ticket_booking.auth.controller;

import com.nghiatr.ticket_booking.auth.dto.AuthResponse;
import com.nghiatr.ticket_booking.auth.dto.LoginRequest;
import com.nghiatr.ticket_booking.auth.dto.RegisterRequest;
import com.nghiatr.ticket_booking.auth.service.AuthService;
import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import com.nghiatr.ticket_booking.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Xác thực người dùng bằng email và mật khẩu, trả về token truy cập và thông tin người dùng.
     *
     * @param request thông tin đăng nhập bao gồm email và mật khẩu
     * @return phản hồi HTTP chứa ApiResponse với thông tin xác thực AuthResponse
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request),"Đăng nhập thành công"));
    }

    /**
     * Đăng ký tài khoản người dùng mới vào hệ thống.
     *
     * @param request thông tin đăng ký bao gồm họ tên, email, mật khẩu và vai trò người dùng
     * @return phản hồi HTTP chứa ApiResponse với thông tin xác thực sau khi đăng ký
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request), "Đăng ký người dùng thành công"));
    }

    /**
     * Cấp lại access token mới từ refresh token hợp lệ.
     *
     * @param refreshToken chuỗi refresh token hiện tại
     * @return phản hồi HTTP chứa AuthResponse với access token mới
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    /**
     * Đăng xuất người dùng hiện tại và thu hồi các refresh token liên quan.
     *
     * @param user thực thể người dùng đã được xác thực gửi yêu cầu
     * @return phản hồi HTTP trạng thái 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        authService.logout(user.getId());
        return ResponseEntity.noContent().build();
    }
}
