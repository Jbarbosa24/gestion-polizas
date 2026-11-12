package com.evaluacion.polizas.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final String apiKey;

    public ApiKeyFilter(@Value("${app.api-key}") String apiKey) { this.apiKey = apiKey; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String recibida = request.getHeader("x-api-key");
        if (recibida == null || !MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8), recibida.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"API_KEY_INVALIDA\",\"mensaje\":\"Se requiere una API Key válida\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
