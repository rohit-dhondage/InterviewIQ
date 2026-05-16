package com.example.rohit.InterviewIQ.Repository;

import com.example.rohit.InterviewIQ.Model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    // Additional query methods can be added if needed
}
