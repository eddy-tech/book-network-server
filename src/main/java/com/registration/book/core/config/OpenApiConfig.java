package com.registration.book.core.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Koko",
                        email = "contact@kokocoding.com",
                        url = "https://kokocoding.com"
                ),
                description = "Book Network API",
                title = "Open API specification - Koko",
                version = "1.0.0",
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                ),
                termsOfService = "https://kokocoding.com/terms-of-service"
        ),
        servers = {
                @Server(
                        description = "Local Env",
                        url = "http://localhost:8088/api/v1"
                ),
                @Server(
                        description = "Production Env",
                        url = "https://book-network.herokuapp.com/api/v1"
                )
        },
        security = {
                @SecurityRequirement(
                        name = "bearer token"
                )
        }
)
@SecurityScheme(
        name = "bearer token",
        description = "JWT auth description",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
