package com.nghiatr.ticket_booking.auth.security;

import com.nghiatr.ticket_booking.shared.utils.ResponseUtil;
import com.nghiatr.ticket_booking.user.model.CustomUserDetails;
import com.nghiatr.ticket_booking.user.service.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Filter all the request with bearer Tokens.
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final String[] UNSECURED_URLS = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/error"
    };

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JWTService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        for (String url : UNSECURED_URLS) {
            if(path.startsWith(url)) {
                return true; // Bỏ qua JWT Filter cho API này.
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
            ) throws ServletException, IOException {

        final String authHeader = request.getHeader(AUTH_HEADER);

        if(authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(BEARER_PREFIX.length());
        try {
            final String userEmail = jwtService.extractEmail(jwt);

            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Perform create the UserDetails and push to context for later use.

                CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(userEmail);

                if(jwtService.isTokenValid(jwt, userDetails.getUser())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            ResponseUtil.writeErrorResponse(
                    request,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    "JWT_EXPIRED",
                    "Phiên đăng nhập đã hết hạn, vui lòng làm mới token."
            );

            return;
        } catch (JwtException e) {
            ResponseUtil.writeErrorResponse(
                    request,
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized",
                    "JWT_INVALID",
                    "Token không hợp lệ"
            );

            return;
        }

        filterChain.doFilter(request, response);

    }
}
