package com.example.rohit.InterviewIQ.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

 @Id
 @GeneratedValue
  private Long id;

 private String name;
 private String email;
 private  String password;
}
