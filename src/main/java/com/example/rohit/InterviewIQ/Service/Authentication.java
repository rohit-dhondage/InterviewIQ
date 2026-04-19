package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.DTO.LoginRequest;
import com.example.rohit.InterviewIQ.Model.User;
import com.example.rohit.InterviewIQ.Repository.UserRepository;
import com.example.rohit.InterviewIQ.Util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class Authentication {
    UserRepository userRepository;
PasswordEncoder passwordEncoder;
JwtUtil jwtUtil;
    Authentication(UserRepository userRepository, PasswordEncoder passwordEncoder ,JwtUtil jwtUtil) {
        this.userRepository=userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;

    }


    public User regester(User user) {

user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);

    }

    public String login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByemail(loginRequest.getEmail());

        if (userOptional.isEmpty()){
            throw new RuntimeException("User Not Found");
        }
        User user = userOptional.get();

        if (!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid Password");
        }

        return jwtUtil.generatetoken(loginRequest.getEmail());
    }
}
