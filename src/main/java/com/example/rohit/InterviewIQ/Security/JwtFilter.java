package com.example.rohit.InterviewIQ.Security;

import com.example.rohit.InterviewIQ.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {

    JwtUtil jwtUtil;
    JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authentication");
        if(header != null && header.startsWith("Bearer ")) {

            // read header ,
            String token = header.substring(7);

            //extract email
            String email = jwtUtil.extractEmail(token);

            if (email != null) {
                // We will attach user to security context (next step)
                System.out.println("Valid token for: " + email);
            }
        }
        filterChain.doFilter(request, response);

    }
}
