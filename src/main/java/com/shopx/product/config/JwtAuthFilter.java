package com.shopx.product.config;

import com.shopx.product.core.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * @author Sameer Shaikh
 * @date 03-04-2026
 * @description
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("User service JWT filter invoked.");

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        Long userId = null;
        List<String> roles=null;
        try {
            userId = jwtUtil.extractUserId(token);
            roles = jwtUtil.extractRoles(token);

            log.info("Extracted userId : {}",userId);
        } catch (Exception e) {
            log.error("Exception : {}",e.getMessage());
            request.setAttribute("exception", e.getMessage());
        }

        if (request.getAttribute("exception") != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userId != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            request.setAttribute(Constants.SESSION_USER_ID,userId);
            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            if (jwtUtil.validateToken(token)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                authorities
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);

    }
}
