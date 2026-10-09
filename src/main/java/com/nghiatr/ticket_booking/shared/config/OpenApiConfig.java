package com.nghiatr.ticket_booking.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * Cấu hình thông tin tài liệu OpenAPI và cơ chế xác thực JWT Bearer cho Swagger UI.
     *
     * @return đối tượng OpenAPI đã được thiết lập thông tin hệ thống và bảo mật
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticket Booking API Documentation")
                        .version("1.0.0")
                        .description("Tài liệu API chính thức cho hệ thống đặt vé xem sự kiện (Ticket Booking System).")
                        .contact(new Contact()
                                .name("Ticket Booking Development Team")
                                .email("contact@ticketbooking.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập JWT Access Token (không cần tiền tố 'Bearer ')")));
    }
}
