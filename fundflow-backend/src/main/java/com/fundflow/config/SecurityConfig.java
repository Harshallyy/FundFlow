package com.fundflow.config;

import com.fundflow.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Frontend is plain HTML/CSS/JS served separately from the backend (different
     * origin/port), so the browser needs an explicit CORS allow-list. Loosened only
     * for local dev origins - tighten this before any real deployment.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---- Public ----
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/campaigns/explore").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/campaigns/*/updates").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/ai/assistant").permitAll()

                // ---- Organizer-only ----
                .requestMatchers(HttpMethod.POST, "/api/ai/generate-description").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.GET, "/api/campaigns/mine").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST, "/api/campaigns").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.PUT, "/api/campaigns/*").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST, "/api/campaigns/*/submit").hasRole("ORGANIZER")
                .requestMatchers(HttpMethod.POST, "/api/campaigns/*/updates").hasRole("ORGANIZER")
                .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")

                // ---- Admin-only ----
                .requestMatchers("/api/campaigns/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/campaigns/*/review").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/campaigns/*/complete").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ---- Donor-only ----
                .requestMatchers(HttpMethod.POST, "/api/donations").hasRole("DONOR")
                .requestMatchers(HttpMethod.GET, "/api/donations/my").hasRole("DONOR")
                .requestMatchers(HttpMethod.GET, "/api/donations/stats").hasRole("DONOR")
                .requestMatchers("/api/saved-campaigns/**").hasRole("DONOR")

                // ---- Organizer or admin (donations received on a campaign) ----
                .requestMatchers(HttpMethod.GET, "/api/donations/campaign/*").hasAnyRole("ORGANIZER", "ADMIN")

                // Public campaign details/browse must come AFTER the more specific
                // /admin and /mine matchers above, since "/api/campaigns/*" would
                // otherwise also match those paths.
                .requestMatchers(HttpMethod.GET, "/api/campaigns/*").permitAll()

                // ---- Everything else just needs to be logged in ----
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
