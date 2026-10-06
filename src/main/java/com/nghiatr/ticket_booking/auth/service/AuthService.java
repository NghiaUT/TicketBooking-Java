package com.nghiatr.ticket_booking.auth.service;

import com.nghiatr.ticket_booking.auth.dto.AuthResponse;
import com.nghiatr.ticket_booking.auth.dto.LoginRequest;
import com.nghiatr.ticket_booking.auth.dto.RegisterRequest;
import com.nghiatr.ticket_booking.auth.entity.RefreshToken;
import com.nghiatr.ticket_booking.auth.exception.AuthErrorCode;
import com.nghiatr.ticket_booking.auth.repository.RefreshTokenRepository;
import com.nghiatr.ticket_booking.auth.security.JWTService;
import com.nghiatr.ticket_booking.shared.exception.AppException;
import com.nghiatr.ticket_booking.user.model.User;
import com.nghiatr.ticket_booking.user.model.UserRole;
import com.nghiatr.ticket_booking.user.repository.UserRepository;
import com.nghiatr.ticket_booking.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
            throw new AppException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserRole role = request.getRole();

        if(role == UserRole.ADMIN) throw new AppException(AuthErrorCode.ADMIN_REGISTER_FORBIDDEN);

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
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new AppException(AuthErrorCode.BAD_CREDENTIALS);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

        // Deactivate tất cả refreshToken của user này còn hạn.
        refreshTokenRepository.revokeAllByUserId(user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(Instant.now())) {
            throw new AppException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new AppException(AuthErrorCode.USER_NOT_FOUND));

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
