package com.webapp.config;

import com.webapp.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsServiceImpl);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/login_admin", "/login_student", "/auth/authenticate","/auth/refresh", "/register",
                                "/favicon.ico", "/error",
                                "/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/student/**").hasAuthority("ROLE_STUDENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(userDetailsServiceImpl), UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home")
                        .deleteCookies("JWT")
                        .permitAll()
                );

        return http.build();
    }
}
