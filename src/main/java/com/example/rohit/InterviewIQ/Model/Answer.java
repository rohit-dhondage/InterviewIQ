package com.example.rohit.InterviewIQ.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Answer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String transcript; // text answer or transcribed audio

    @Lob
    private byte[] audioBlob; // original audio if provided

    private Double score; // LLM evaluation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
