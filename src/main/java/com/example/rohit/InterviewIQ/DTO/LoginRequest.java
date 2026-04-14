package com.example.rohit.InterviewIQ.DTO;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    @Column(unique = true)
    String email;
    String password;
}
