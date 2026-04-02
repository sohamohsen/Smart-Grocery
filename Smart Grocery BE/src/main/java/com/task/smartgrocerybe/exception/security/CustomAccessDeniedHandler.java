package com.task.smartgrocerybe.exception.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.smartgrocerybe.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {

        try {

            ApiResponse<?> apiResponse =
                    ApiResponse.error("You don't have permission to access this resource");

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");

            response.getWriter()
                    .write(objectMapper.writeValueAsString(apiResponse));

        } catch (Exception e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }
}