package com.webapp.config;

import com.webapp.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;  // Add this import for handling expired tokens
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private JwtUtil jwtUtil = new JwtUtil();

    public JwtAuthenticationFilter(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        String username = null;

        // 1. Check Authorization header first
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }
        // 2. If not in header, check cookies
        else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JWT".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                System.out.println("JWT expired while extracting username: " + e.getMessage());
                // Optionally: you can set a request attribute or header here if needed
            } catch (Exception e) {
                System.out.println("Error extracting username from JWT: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            try {
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                System.out.println("JWT expired during validation: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("JWT validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

}
