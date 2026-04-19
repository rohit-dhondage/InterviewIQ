package com.example.rohit.InterviewIQ.Security;

import com.example.rohit.InterviewIQ.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    JwtUtil jwtUtil;
    JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if(header != null && header.startsWith("Bearer ")) {

            // read header ,
            String token = header.substring(7);

            //extract email
            try {
                String email = jwtUtil.extractEmail(token);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("USER"))
                        );
                System.out.println("done   ");

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } catch (Exception e) {
                // 🔥 IMPORTANT: just ignore invalid token
                System.out.println("Invalid JWT token");
            }
        }
        filterChain.doFilter(request, response);

    }
}
