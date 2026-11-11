package com.surveillance.robot.config;

import com.surveillance.robot.security.ApiKeyAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RobotProperties robotProps) throws Exception {
        http
                .csrf(csrf -> csrf.disable())   // stateless API, no browser session cookies
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/robot/**").hasRole("ROBOT")
                        .requestMatchers(HttpMethod.POST, "/api/robots").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("ADMIN", "OPERATOR")
                        .anyRequest().hasAnyRole("ADMIN", "OPERATOR"))   // dashboard pages
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(new ApiKeyAuthFilter(robotProps.apiKey()), BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
