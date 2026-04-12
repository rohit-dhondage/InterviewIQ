package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.Model.User;
import com.example.rohit.InterviewIQ.Repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class Authentication {
    UserRepository userRepository;

    Authentication(UserRepository userRepository  ) {
        this.userRepository=userRepository;

    }

    public User regester(User user) {

        return userRepository.save(user);

    }
}
