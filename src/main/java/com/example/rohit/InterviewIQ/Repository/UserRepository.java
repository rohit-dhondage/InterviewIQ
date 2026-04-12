package com.example.rohit.InterviewIQ.Repository;

import com.example.rohit.InterviewIQ.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User , Long> {
    Optional<User> findByemail(String email);
}
