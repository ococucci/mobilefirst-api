package com.orderfirst.api.config;

import com.orderfirst.api.domain.ApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

@Configuration
public class WebConfig {
    /** Origines autorisées à appeler l'API (le front Angular en développement par défaut). */
    @ConfigurationProperties("orderfirst.cors")
    public record CorsProperties(List<String> allowedOrigins) {
        public CorsProperties {
            allowedOrigins = allowedOrigins == null ? List.of("http://localhost:4200") : allowedOrigins;
        }
    }

    @Bean
    WebMvcConfigurer cors(final CorsProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(final CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(properties.allowedOrigins().toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT");
            }
        };
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Orange Order First API")
                .version("v1")
                .description("API de la prise de commande mobile en boutique (acquisition, changement de forfait, "
                        + "options et SIM). Les API du SI sont bouchonnées tant que orderfirst.integrations=stub."));
    }

    /** Toutes les erreurs sortent en ProblemDetail ; les erreurs de saisie listent chaque champ dans « errors ». */
    @RestControllerAdvice
    static class Errors {
        @ExceptionHandler(ApiException.class)
        ProblemDetail api(final ApiException e) {
            final var problem = ProblemDetail.forStatusAndDetail(e.status(), e.getMessage());
            problem.setTitle(e.title());
            if (!e.fieldErrors().isEmpty()) {
                problem.setProperty("errors", e.fieldErrors().entrySet().stream()
                        .map(en -> Map.of("field", en.getKey(), "message", en.getValue()))
                        .toList());
            }
            return problem;
        }

        @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
        ProblemDetail unreadable(final Exception e) {
            final var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Requête invalide : " + e.getMessage());
            problem.setTitle("Requête invalide");
            return problem;
        }
    }
}
