package com.nghiatr.ticket_booking.auth.controller;

import com.nghiatr.ticket_booking.auth.dto.AuthResponse;
import com.nghiatr.ticket_booking.auth.dto.LoginRequest;
import com.nghiatr.ticket_booking.auth.dto.RegisterRequest;
import com.nghiatr.ticket_booking.auth.service.AuthService;
import com.nghiatr.ticket_booking.shared.dto.ApiResponse;
import com.nghiatr.ticket_booking.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Authentication", description = "Quản lý xác thực người dùng, đăng ký, đăng nhập và cấp mới JWT")
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
    @Operation(summary = "Đăng nhập", description = "Xác thực người dùng bằng email và mật khẩu, trả về access token và refresh token")
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
    @Operation(summary = "Đăng ký tài khoản", description = "Đăng ký tài khoản người dùng mới vào hệ thống")
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
    @Operation(summary = "Làm mới access token", description = "Cấp lại access token mới từ refresh token hợp lệ")
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Parameter(description = "Chuỗi Refresh Token hiện tại", required = true) @RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    /**
     * Đăng xuất người dùng hiện tại và thu hồi các refresh token liên quan.
     *
     * @param user thực thể người dùng đã được xác thực gửi yêu cầu
     * @return phản hồi HTTP trạng thái 204 No Content
     */
    @Operation(summary = "Đăng xuất", description = "Đăng xuất người dùng hiện tại và thu hồi các refresh token liên quan", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        authService.logout(user.getId());
        return ResponseEntity.noContent().build();
    }
}
