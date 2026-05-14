package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.Exception.ResourceNotFoundException;
import com.example.rohit.InterviewIQ.Model.InterviewSession;
import com.example.rohit.InterviewIQ.Model.User;
import com.example.rohit.InterviewIQ.Repository.InterviewSessionRepository;
import com.example.rohit.InterviewIQ.Repository.UserRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class InterviewService {

    private final ChatModel chatModel;
    private final UserRepository userRepository;
    private final InterviewSessionRepository sessionRepository;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    public InterviewService(ChatModel chatModel, UserRepository userRepository, InterviewSessionRepository sessionRepository) {
        this.chatModel = chatModel;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByemail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public InterviewSession generateQuestions(String resumeText) {
        String prompt = "You are an expert technical interviewer. Based on the following resume text, " +
                "generate 5 specific, challenging interview questions that test the candidate's actual experience and projects.\n\n" +
                "Candidate Resume:\n" + resumeText;
        
        String generatedQuestions = chatModel.call(prompt);

        // Save session to database
        User user = getAuthenticatedUser();
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .resumeText(resumeText)
                .generatedQuestions(generatedQuestions)
                .build();

        return sessionRepository.save(session);
    }

    public String transcribeAudioAndEvaluate(Long sessionId, MultipartFile audioFile) throws Exception {
        // 1. Transcribe the Audio using OpenAI Whisper API directly
        String transcript = transcribeWithWhisper(audioFile);

        // 2. Fetch the session
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        
        // 3. Save Transcript
        session.setUserAnswerTranscript(transcript);

        // 4. Generate AI Feedback based on the answers
        String feedbackPrompt = "You are an expert technical interviewer. The candidate was asked these questions:\n" 
                + session.getGeneratedQuestions() + "\n\n"
                + "The candidate provided the following spoken answer:\n" + transcript + "\n\n"
                + "Provide constructive feedback on their answer. What did they do well? What could be improved? Give them a score out of 10.";
        
        String feedback = chatModel.call(feedbackPrompt);
        session.setAiFeedback(feedback);

        sessionRepository.save(session);
        return feedback;
    }

    private String transcribeWithWhisper(MultipartFile audioFile) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openAiApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audioFile.getResource());
        body.add("model", "whisper-1");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<java.util.Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/audio/transcriptions",
                requestEntity,
                java.util.Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("text");
        } else {
            throw new RuntimeException("Failed to transcribe audio");
        }
    }
}
