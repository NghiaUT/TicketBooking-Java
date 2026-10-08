package com.nghiatr.ticket_booking.shared.config;

import com.nghiatr.ticket_booking.auth.security.JwtAuthenticationFilter;
import com.nghiatr.ticket_booking.shared.handler.CustomAccessDeniedHandler;
import com.nghiatr.ticket_booking.shared.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter; // Đưa filter vừa tạo vào
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * Cấu hình chuỗi bộ lọc bảo mật SecurityFilterChain của ứng dụng (CSRF, phân quyền URL, session stateless).
     *
     * @param http đối tượng HttpSecurity để thiết lập cấu hình
     * @return đối tượng SecurityFilterChain hoàn chỉnh
     * @throws Exception nếu xảy ra lỗi trong quá trình cấu hình
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                jwtAuthFilter.UNSECURED_URLS
                        ).permitAll()// Mở cổng không sử dụng xác thực cho các API này

                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers("/api/v1/orders/**").hasRole("CUSTOMER")

                        .anyRequest().authenticated()                // Các API còn lại của hệ thống
                )
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Cấu hình 100% Stateless
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler) // Tự cấu hình mã lỗi 401 và 403 để trả về có message.
                )
                // Đặt JWT Filter CHẠY TRƯỚC Filter xác thực mặc định của Spring
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cung cấp bộ mã hóa mật khẩu sử dụng giải thuật BCrypt với độ mạnh (strength) là 10.
     *
     * @return đối tượng PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Cung cấp Bean AuthenticationManager từ cấu hình bảo mật của Spring Security.
     *
     * @param config cấu hình xác thực AuthenticationConfiguration
     * @return đối tượng AuthenticationManager
     * @throws Exception nếu không thể khởi tạo AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
