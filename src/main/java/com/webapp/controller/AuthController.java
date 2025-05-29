package com.webapp.controller;

import com.webapp.config.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/redirect-by-role")
    public String redirectByRole() {
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
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

            // Role check
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

            // Generate JWT token
            final String jwt = jwtUtil.generateToken(userDetails);
            Cookie jwtCookie = new Cookie("JWT", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Change to true in production (HTTPS)
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60 * 24); // 24 hours
            response.addCookie(jwtCookie);

            // Redirect based on role
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
