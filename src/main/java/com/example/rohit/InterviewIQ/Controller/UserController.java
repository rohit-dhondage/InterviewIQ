package com.example.rohit.InterviewIQ.Controller;

import com.example.rohit.InterviewIQ.DTO.LoginRequest;
import com.example.rohit.InterviewIQ.Model.User;
import com.example.rohit.InterviewIQ.Service.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/auth")
public class UserController {
    Authentication authentication;
    UserController(Authentication authentication){
        this.authentication= authentication;
    }

    @PostMapping("/register")
    public ResponseEntity<User> regester(@RequestBody User user){
        authentication.regester(user);
        return   ResponseEntity.ok(user);
    }


    @PutMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {
       return authentication.login(loginRequest);
    }


    @GetMapping("/test")
    public String test() {
        return "Protected API working";
    }
}
