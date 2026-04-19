package com.example.rohit.InterviewIQ.Util;

import com.example.rohit.InterviewIQ.DTO.LoginRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.Date;

import static java.security.KeyRep.Type.SECRET;



@Component public class JwtUtil {
    private final String secretkey = "myverystrongsecretkeythatshouldbeatleast32characterslong";

    public String generatetoken(String email){
return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .signWith(SignatureAlgorithm.HS256, secretkey)
        .compact();

    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .setSigningKey(secretkey)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
