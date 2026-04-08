package com.example.rejunkfrontend.security;

import com.example.rejunkfrontend.dto.AuthResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Detects stale sessions (null token or class-loader mismatch after hot-reload)
 * and redirects the user to /login so they can re-authenticate.
 */
@Component
public class TokenContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object attr = session.getAttribute("user");
            if (attr != null && !(attr instanceof AuthResponse)) {
                log.warn("Stale session (class-loader mismatch: {}); redirecting to login",
                        attr.getClass().getName());
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (attr instanceof AuthResponse user && user.token() == null) {
                log.warn("Session user '{}' has null token; redirecting to login", user.email());
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
