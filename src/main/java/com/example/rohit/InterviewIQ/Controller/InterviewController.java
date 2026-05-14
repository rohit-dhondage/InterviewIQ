package com.example.rohit.InterviewIQ.Controller;

import com.example.rohit.InterviewIQ.Model.InterviewSession;
import com.example.rohit.InterviewIQ.Service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * Accepts resume text, generates AI questions, and saves the session to the DB linked to the User.
     */
    @PostMapping("/generate-questions")
    public ResponseEntity<?> generateQuestions(@RequestBody Map<String, String> payload) throws Exception {
        String resumeText = payload.get("resumeText");
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: resumeText is required in the request body.");
        }

        InterviewSession session = interviewService.generateQuestions(resumeText);
        return ResponseEntity.ok(session); // Returns the session with ID and Questions
    }

    /**
     * Accepts a voice recording from the user, transcribes it, and evaluates it.
     */
    @PostMapping("/{sessionId}/answer-voice")
    public ResponseEntity<?> answerWithVoice(
            @PathVariable Long sessionId,
            @RequestParam("audioFile") MultipartFile audioFile) throws Exception {
        
        String feedback = interviewService.transcribeAudioAndEvaluate(sessionId, audioFile);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }
}
