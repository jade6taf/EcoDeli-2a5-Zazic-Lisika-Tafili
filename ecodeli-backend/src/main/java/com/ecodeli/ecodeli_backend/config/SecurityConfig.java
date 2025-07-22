package com.ecodeli.ecodeli_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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

import com.ecodeli.ecodeli_backend.security.JwtRequestFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/addresses/**").permitAll()
                .requestMatchers("/api/admin/documents/view/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/planning/**").hasRole("PRESTATAIRE")
                .requestMatchers("/api/documents/upload").authenticated()
                .anyRequest().authenticated())
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // URLs de développement local
        configuration.addAllowedOrigin("http://localhost:5173");
        configuration.addAllowedOrigin("http://localhost:5174");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:5173");
        configuration.addAllowedOrigin("http://127.0.0.1:5174");
        
        // URLs Railway spécifiques (le wildcard ne fonctionne pas)
        configuration.addAllowedOrigin("https://ecodeli-2a5-zazic-lisika-tafili-production.up.railway.app");
        configuration.addAllowedOrigin("https://mindful-reprieve-production.up.railway.app");
        
        // Variables d'environnement pour URLs personnalisées
        String frontendUserUrl = System.getenv("FRONTEND_USER_URL");
        String frontendAdminUrl = System.getenv("FRONTEND_ADMIN_URL");
        
        if (frontendUserUrl != null && !frontendUserUrl.isEmpty()) {
            configuration.addAllowedOrigin(frontendUserUrl);
        }
        
        if (frontendAdminUrl != null && !frontendAdminUrl.isEmpty()) {
            configuration.addAllowedOrigin(frontendAdminUrl);
        }
        
        // Pour le développement et la production Railway, autoriser tous les patterns Railway
        String railwayEnv = System.getenv("RAILWAY_ENVIRONMENT");
        if (railwayEnv != null || System.getenv("NODE_ENV") == null) {
            // Mode développement - plus permissif
            configuration.setAllowCredentials(true);
            configuration.addAllowedOriginPattern("https://*.railway.app");
            configuration.addAllowedOriginPattern("https://*.up.railway.app");
            // Patterns spécifiques pour les frontends Railway
            configuration.addAllowedOriginPattern("https://ecodeli-frontend-user-production-*.up.railway.app");
            configuration.addAllowedOriginPattern("https://ecodeli-frontend-admin-production-*.up.railway.app");
        }
        
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.addExposedHeader("Content-Disposition");
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
