package com.shopx.product.handlers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Sameer Shaikh
 * @date 06-04-2026
 * @description
 */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(
                HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException
        ) throws IOException {

            String ex = (String) request.getAttribute("exception");
            String message = (ex==null)?"Something went wrong.":ex;

            if(authException!=null && authException.getMessage()!=null)
                message=authException.getMessage();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            String body = """
                {
                  "status": 401,
                  "error": "UNAUTHORIZED",
                  "message": "%s"
                }
                """.formatted(message);

            response.getWriter().write(body);
        }
}
