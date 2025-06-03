package com.webapp.controller;
import com.webapp.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.webapp.dto.AuthResponse;

import java.util.Map;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        String refreshToken = null;

        // Get from cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken != null) {
            try {
                String username = jwtUtil.extractUsername(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(refreshToken, userDetails)) {
                    String newAccessToken = jwtUtil.generateAccessToken(userDetails);

                    return ResponseEntity.ok()
                            .header("Authorization", "Bearer " + newAccessToken)
                            .body(Map.of("accessToken", newAccessToken));
                }

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    @GetMapping("/redirect-by-role")
    public String redirectByRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/student/details";
        }
    }

    @PostMapping("/authenticate")
    public String authenticate(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String expectedRole,
            HttpServletResponse response,
            Model model
    ) {
        System.out.println("Login attempt for: " + username + " expecting role: " + expectedRole);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            System.out.println("Authenticated: " + auth.getName());
            System.out.println("Roles: " + auth.getAuthorities());

            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean hasExpectedRole = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(expectedRole));

            if (!hasExpectedRole) {
                System.out.println("Role mismatch for user: " + username);
                if ("ROLE_ADMIN".equals(expectedRole)) {
                    return "redirect:/login_admin?error=role";
                } else {
                    return "redirect:/login_student?error=role";
                }
            }

            final String jwt = jwtUtil.generateToken(userDetails);
            final String refreshToken = jwtUtil.generateRefreshToken(userDetails); // You need this method implemented in JwtUtil

            // JWT Cookie
            Cookie jwtCookie = new Cookie("JWT", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 10); // 10 hours
            response.addCookie(jwtCookie);

            // Refresh Token Cookie
            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false); // Set to true in production with HTTPS
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(refreshCookie);

            if ("ROLE_ADMIN".equals(expectedRole)) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/student/details";
            }

        } catch (BadCredentialsException e) {
            System.out.println("Login failed: " + e.getMessage());

            if ("ROLE_ADMIN".equals(expectedRole)) {
                return "redirect:/login_admin?error=true";
            } else {
                return "redirect:/login_student?error=true";
            }
        }
    }
}
