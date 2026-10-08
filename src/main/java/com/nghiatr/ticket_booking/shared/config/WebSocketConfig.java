package com.nghiatr.ticket_booking.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Cấu hình Message Broker cho các kênh nhận/gửi tin nhắn WebSocket STOMP.
     *
     * @param registry đối tượng MessageBrokerRegistry để đăng ký tiền tố broker
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 1. Prefix cho các topic mà client sẽ SUBSCRIBE để nhận broadcast
        // vd: /topic/events/{eventId}/seats
        registry.enableSimpleBroker("/topic", "/queue");

        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Đăng ký endpoint STOMP kết nối WebSocket cho client.
     *
     * @param registry đối tượng StompEndpointRegistry để cấu hình endpoint và CORS
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép frontend React kết nối.
                .withSockJS();

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // endpoint thuần cho client không dùng SockJS.
    }
}
