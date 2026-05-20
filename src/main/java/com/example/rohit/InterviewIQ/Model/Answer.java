package com.example.rohit.InterviewIQ.Model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(columnDefinition = "TEXT")
    private String feedback; // AI feedback on this specific answer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    @JsonIgnore
    private Question question;
}
