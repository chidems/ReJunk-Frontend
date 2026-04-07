package com.example.rejunkfrontend.security;

import com.example.rejunkfrontend.dto.AuthResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenContextFilter extends OncePerRequestFilter {

    private final TokenHolder tokenHolder;

    public TokenContextFilter(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            AuthResponse user = (AuthResponse) session.getAttribute("user");
            if (user != null && user.token() != null) {
                tokenHolder.set(user.token());
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            tokenHolder.clear();
        }
    }
}