package com.example.rohit.InterviewIQ.Controller;

import com.example.rohit.InterviewIQ.Model.InterviewSession;
import com.example.rohit.InterviewIQ.Model.Question;
import com.example.rohit.InterviewIQ.Model.Answer;
import com.example.rohit.InterviewIQ.Service.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    /**
     * Starts a new conversational interview session by accepting resume text and
     * returning the session containing ONLY the first interview question.
     */
    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody Map<String, String> payload) {
        String resumeText = payload.get("resumeText");
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: resumeText is required in the request body.");
        }

        InterviewSession session = interviewService.startInterviewSession(resumeText);
        return ResponseEntity.ok(session);
    }

    /**
     * Accepts a voice recording for the current question, evaluates it, saves it,
     * and returns the evaluation feedback alongside the next generated question (if any).
     */
    @PostMapping("/{sessionId}/questions/{questionId}/submit-answer")
    public ResponseEntity<?> submitAnswerAndGetNext(
            @PathVariable Long sessionId,
            @PathVariable Long questionId,
            @RequestParam("audioFile") MultipartFile audioFile) {
        
        Map<String, Object> nextStep = interviewService.submitAnswerAndGetNext(sessionId, questionId, audioFile);
        return ResponseEntity.ok(nextStep);
    }

    /**
     * Retrieves the complete summary evaluation report of the interview session.
     */
    @GetMapping("/{sessionId}/report")
    public ResponseEntity<?> getSessionReport(@PathVariable Long sessionId) {
        Map<String, Object> report = interviewService.getSessionReport(sessionId);
        return ResponseEntity.ok(report);
    }

    /**
     * Retrieves the individual questions generated for a specific interview session.
     */
    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable Long sessionId) {
        List<Question> questions = interviewService.getQuestionsForSession(sessionId);
        return ResponseEntity.ok(questions);
    }

    /**
     * Legacy endpoint - Accepts resume text, generates 5 AI questions at once, and saves the session.
     */
    @PostMapping("/generate-questions")
    public ResponseEntity<?> generateQuestions(@RequestBody Map<String, String> payload) {
        String resumeText = payload.get("resumeText");
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: resumeText is required in the request body.");
        }

        InterviewSession session = interviewService.generateQuestions(resumeText);
        return ResponseEntity.ok(session);
    }

    /**
     * Legacy endpoint - Accepts a voice recording from the user, transcribes it, and evaluates it.
     */
    @PostMapping("/{sessionId}/answer-voice")
    public ResponseEntity<?> answerWithVoice(
            @PathVariable Long sessionId,
            @RequestParam("audioFile") MultipartFile audioFile) {
        
        String feedback = interviewService.transcribeAudioAndEvaluate(sessionId, audioFile);
        return ResponseEntity.ok(Map.of("feedback", feedback));
    }
}
