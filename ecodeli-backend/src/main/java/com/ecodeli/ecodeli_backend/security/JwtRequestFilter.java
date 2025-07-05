package com.ecodeli.ecodeli_backend.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String jwt = null;
        String email = null;

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        }

        if (jwt == null) {
            jwt = request.getParameter("token");
        }
        if (jwt != null) {
            try {
                email = jwtUtil.extractEmail(jwt);
                logger.info("JWT trouvé pour email: " + email);
            } catch (Exception e) {
                logger.error("Erreur lors de l'extraction du JWT", e);
            }
        } else {
            logger.info("Aucun JWT trouvé dans la requête: " + request.getRequestURI());
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                String userType = jwtUtil.extractUserType(jwt);
                logger.info("Token valide pour email: " + email + ", userType: " + userType + ", role créé: ROLE_" + userType);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    email, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authentification réussie pour: " + email + " avec rôle: ROLE_" + userType);
            } else {
                logger.warn("Token JWT invalide ou expiré pour email: " + email);
            }
        } else if (email == null) {
            logger.info("Email null, pas d'authentification pour: " + request.getRequestURI());
        } else {
            logger.info("Authentification déjà présente pour: " + email);
        }
        chain.doFilter(request, response);
    }
}
