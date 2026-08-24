package com.nghiatr.ticket_booking.auth.service;

import com.nghiatr.ticket_booking.auth.dto.AuthResponse;
import com.nghiatr.ticket_booking.auth.dto.LoginRequest;
import com.nghiatr.ticket_booking.auth.dto.RegisterRequest;
import com.nghiatr.ticket_booking.auth.entity.RefreshToken;
import com.nghiatr.ticket_booking.auth.repository.RefreshTokenRepository;
import com.nghiatr.ticket_booking.auth.security.JWTService;
import com.nghiatr.ticket_booking.shared.dto.ErrorCode;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.user.model.User;
import com.nghiatr.ticket_booking.user.model.UserRole;
import com.nghiatr.ticket_booking.user.repository.UserRepository;
import com.nghiatr.ticket_booking.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        UserRole role = request.getRole();

        if(role == UserRole.ADMIN) throw new IllegalArgumentException("Không được đăng ký bằng admin");

        User user = userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                passwordEncoder,
                role
        );

        switch (role) {
            case CUSTOMER :
                userService.createCustomer(user);
                break;
            case ORGANIZER:
                userService.createOrganizer(user);
                break;
        }

        return buildAuthResponse(user);
    }



    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        // Deactivate tất cả refreshToken của user này còn hạn.
        refreshTokenRepository.revokeAllByUserId(user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không hợp lệ"));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token đã hết hạn hoặc bị thu hồi");
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(storedToken.getToken())
                .build();
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .ExpiresIn(jwtService.getAccessTokenExpirationMs())
                .build();
    }
}
