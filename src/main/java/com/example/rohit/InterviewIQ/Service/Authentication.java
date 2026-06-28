package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.DTO.LoginRequest;
import com.example.rohit.InterviewIQ.Exception.ResourceNotFoundException;
import com.example.rohit.InterviewIQ.Model.User;
import com.example.rohit.InterviewIQ.Repository.UserRepository;
import com.example.rohit.InterviewIQ.Util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Authentication {

    private static final Logger log = LoggerFactory.getLogger(Authentication.class);

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtUtil jwtUtil;

    Authentication(UserRepository userRepository,
                   PasswordEncoder passwordEncoder,
                   JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public User regester(User user) {

        log.info("Registration request received for email: {}", user.getEmail());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        log.info("User registered successfully. User ID: {}", savedUser.getId());

        return savedUser;
    }

    public String login(LoginRequest loginRequest) {

        log.info("Login request received for email: {}", loginRequest.getEmail());

        Optional<User> userOptional =
                userRepository.findByemail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {

            log.warn("Login failed. User not found for email: {}", loginRequest.getEmail());

            throw new ResourceNotFoundException("User Not Found");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {

            log.warn("Login failed. Invalid password for email: {}", loginRequest.getEmail());

            throw new ResourceNotFoundException("Invalid Password");
        }

        String token = jwtUtil.generateToken(loginRequest.getEmail());

        log.info("Login successful for email: {}", loginRequest.getEmail());

        return token;
    }
}