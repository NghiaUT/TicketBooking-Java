package com.nghiatr.ticket_booking.share.utils;

import com.nghiatr.ticket_booking.share.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public class ResponseUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String error,
            String errorCode,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse errorResponse = new ErrorResponse(
                status,
                error,
                errorCode,
                message,
                request.getServletPath(),
                Instant.now()
        );

        OBJECT_MAPPER.writeValue(response.getOutputStream(), errorResponse);
    }
}
