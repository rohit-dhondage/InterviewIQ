package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.Exception.ResourceNotFoundException;
import com.example.rohit.InterviewIQ.Exception.AiServiceException;
import com.example.rohit.InterviewIQ.Exception.TranscriptionException;
import com.example.rohit.InterviewIQ.Model.InterviewSession;
import com.example.rohit.InterviewIQ.Model.User;
import com.example.rohit.InterviewIQ.Model.Question;
import com.example.rohit.InterviewIQ.Model.Answer;
import com.example.rohit.InterviewIQ.Repository.InterviewSessionRepository;
import com.example.rohit.InterviewIQ.Repository.UserRepository;
import com.example.rohit.InterviewIQ.Repository.QuestionRepository;
import com.example.rohit.InterviewIQ.Repository.AnswerRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InterviewService {

    private final ChatModel chatModel;
    private final UserRepository userRepository;
    private final InterviewSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    public InterviewService(ChatModel chatModel,
            UserRepository userRepository,
            InterviewSessionRepository sessionRepository,
            QuestionRepository questionRepository,
            AnswerRepository answerRepository) {
        this.chatModel = chatModel;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByemail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Starts a new interview session and generates the FIRST question based on the
     * resume.
     */
    @Transactional
    public InterviewSession startInterviewSession(String resumeText) {
        User user = getAuthenticatedUser();

        // 1. Create and save the session
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .resumeText(resumeText)
                .build();
        InterviewSession savedSession = sessionRepository.save(session);

        // 2. Generate the first question
        String prompt = "You are an expert technical interviewer. Based on the candidate's resume below, " +
                "generate the first specific, challenging interview question to start the interview. " +
                "Do not include any greetings, introductory remarks, or filler text. Only return the question itself.\n\n"
                +
                "Candidate Resume:\n" + resumeText;

        String firstQuestionPrompt;
        try {
            firstQuestionPrompt = chatModel.call(prompt).trim();
        } catch (Exception e) {
            throw new AiServiceException("Failed to generate interview question using AI", e);
        }

        // 3. Save the first question
        Question question = Question.builder()
                .prompt(firstQuestionPrompt)
                .orderIndex(0)
                .session(savedSession)
                .build();
        Question savedQuestion = questionRepository.save(question);

        savedSession.setQuestions(Collections.singletonList(savedQuestion));
        savedSession.setGeneratedQuestions(firstQuestionPrompt);
        return sessionRepository.save(savedSession);
    }

    /**
     * Submits an answer for the current question, evaluates it, and generates the
     * next question
     * dynamically based on context (up to 5 questions in total).
     */
    @Transactional
    public Map<String, Object> submitAnswerAndGetNext(Long sessionId, Long questionId, MultipartFile audioFile) {
        // 1. Fetch resources
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        Question currentQuestion = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (!currentQuestion.getSession().getId().equals(sessionId)) {
            throw new IllegalArgumentException("Question does not belong to this session");
        }

        // 2. Transcribe voice answer
        String transcript = transcribeWithWhisper(audioFile);

        // 3. Evaluate current answer
        String evalPrompt = "You are an expert technical interviewer. Evaluate the candidate's answer for the following question:\n\n"
                + "Question: " + currentQuestion.getPrompt() + "\n\n"
                + "Candidate's Answer: " + transcript + "\n\n"
                + "Provide feedback in the following format:\n"
                + "Score: <a number between 1.0 and 10.0 representing the accuracy and depth of the answer>\n"
                + "Feedback: <constructive feedback highlighting strengths and areas of improvement>";

        String evalResult;
        try {
            evalResult = chatModel.call(evalPrompt);
        } catch (Exception e) {
            throw new AiServiceException("Failed to evaluate candidate answer using AI", e);
        }

        double score = parseScore(evalResult);
        String feedback = parseFeedback(evalResult);

        // 4. Save the Answer
        byte[] audioBytes;
        try {
            audioBytes = audioFile.getBytes();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read audio file payload", e);
        }

        Answer answer = Answer.builder()
                .question(currentQuestion)
                .transcript(transcript)
                .score(score)
                .feedback(feedback)
                .audioBlob(audioBytes)
                .build();
        Answer savedAnswer = answerRepository.save(answer);

        // 5. Check if we need to generate another question (Limit to 5 questions total)
        List<Question> existingQuestions = questionRepository.findBySessionIdOrderByOrderIndexAsc(sessionId);
        int nextOrderIndex = existingQuestions.size();

        Map<String, Object> response = new HashMap<>();
        response.put("currentAnswer", savedAnswer);

        if (nextOrderIndex < 5) {
            // Build conversation history for the LLM
            StringBuilder historyBuilder = new StringBuilder();
            for (Question q : existingQuestions) {
                historyBuilder.append("Interviewer: ").append(q.getPrompt()).append("\n");
                if (q.getId().equals(questionId)) {
                    historyBuilder.append("Candidate: ").append(transcript).append("\n");
                } else {
                    List<Answer> answers = q.getAnswers();
                    if (answers != null && !answers.isEmpty()) {
                        historyBuilder.append("Candidate: ").append(answers.get(answers.size() - 1).getTranscript())
                                .append("\n");
                    }
                }
            }

            // Generate next question
            String nextPrompt = "You are an expert technical interviewer conducting a structured technical interview. "
                    +
                    "Based on the candidate's resume and the interview conversation history below, generate the next interview question. "
                    +
                    "This is question " + (nextOrderIndex + 1)
                    + " of 5. You can choose to ask a follow-up question about their previous answer, " +
                    "or explore a new topic from their resume. Do not include any greeting, intro, feedback, or outer formatting. Only return the question itself.\n\n"
                    +
                    "Candidate Resume:\n" + session.getResumeText() + "\n\n" +
                    "Interview History:\n" + historyBuilder.toString();

            String nextQuestionPrompt;
            try {
                nextQuestionPrompt = chatModel.call(nextPrompt).trim();
            } catch (Exception e) {
                throw new AiServiceException("Failed to generate next interview question using AI", e);
            }

            // Save next question
            Question nextQuestion = Question.builder()
                    .prompt(nextQuestionPrompt)
                    .orderIndex(nextOrderIndex)
                    .session(session)
                    .build();
            Question savedNextQuestion = questionRepository.save(nextQuestion);

            // Update session's generated questions list for log
            session.setGeneratedQuestions(session.getGeneratedQuestions() + "\n" + nextQuestionPrompt);
            sessionRepository.save(session);

            response.put("nextQuestion", savedNextQuestion);
            response.put("interviewFinished", false);
        } else {
            // All 5 questions have been answered. Compile final report summary.
            response.put("interviewFinished", true);
            compileFinalReport(session);
        }

        return response;
    }

    private void compileFinalReport(InterviewSession session) {
        List<Question> questions = questionRepository.findBySessionIdOrderByOrderIndexAsc(session.getId());
        StringBuilder reportBuilder = new StringBuilder();
        double totalScore = 0;
        int answeredCount = 0;

        for (Question q : questions) {
            List<Answer> ansList = q.getAnswers();
            if (ansList != null && !ansList.isEmpty()) {
                Answer latest = ansList.get(ansList.size() - 1);
                reportBuilder.append("Q: ").append(q.getPrompt()).append("\n");
                reportBuilder.append("A: ").append(latest.getTranscript()).append("\n");
                reportBuilder.append("Score: ").append(latest.getScore()).append("/10\n");
                reportBuilder.append("Feedback: ").append(latest.getFeedback()).append("\n\n");
                totalScore += latest.getScore();
                answeredCount++;
            }
        }

        String summaryPrompt = "You are an expert technical recruiter. Based on the candidate's performance across "
                + answeredCount
                + " questions during the interview session, provide a professional, executive-level summary evaluation of their strengths, weaknesses, and overall job readiness.\n\n"
                + "Interview Details:\n" + reportBuilder.toString();

        String summaryFeedback;
        try {
            summaryFeedback = chatModel.call(summaryPrompt);
        } catch (Exception e) {
            throw new AiServiceException("Failed to generate overall feedback report using AI", e);
        }

        session.setAiFeedback(summaryFeedback);
        sessionRepository.save(session);
    }

    /**
     * Aggregates all answers for a session, calculates average scores,
     * and returns the final report structure.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSessionReport(Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        List<Question> questions = questionRepository.findBySessionIdOrderByOrderIndexAsc(sessionId);

        List<Map<String, Object>> questionAnswerPairs = new ArrayList<>();
        double totalScore = 0;
        int answeredCount = 0;

        for (Question q : questions) {
            Map<String, Object> pair = new HashMap<>();
            pair.put("questionId", q.getId());
            pair.put("prompt", q.getPrompt());
            pair.put("orderIndex", q.getOrderIndex());

            List<Answer> answers = q.getAnswers();
            if (answers != null && !answers.isEmpty()) {
                Answer latestAnswer = answers.get(answers.size() - 1);
                pair.put("answerId", latestAnswer.getId());
                pair.put("transcript", latestAnswer.getTranscript());
                pair.put("score", latestAnswer.getScore());
                pair.put("feedback", latestAnswer.getFeedback());

                totalScore += latestAnswer.getScore();
                answeredCount++;
            } else {
                pair.put("transcript", null);
                pair.put("score", null);
                pair.put("feedback", "Not answered");
            }
            questionAnswerPairs.add(pair);
        }

        double averageScore = answeredCount > 0 ? (totalScore / answeredCount) : 0.0;

        Map<String, Object> report = new HashMap<>();
        report.put("sessionId", session.getId());
        report.put("averageScore", averageScore);
        report.put("summaryFeedback", session.getAiFeedback());
        report.put("questionsAndAnswers", questionAnswerPairs);

        return report;
    }

    /**
     * Legacy method: generates all 5 questions at once.
     */
    @Transactional
    public InterviewSession generateQuestions(String resumeText) {
        User user = getAuthenticatedUser();
        InterviewSession session = InterviewSession.builder()
                .user(user)
                .resumeText(resumeText)
                .build();
        InterviewSession savedSession = sessionRepository.save(session);

        String prompt = "You are an expert technical interviewer. Based on the following resume text, " +
                "generate exactly 5 specific, challenging interview questions that test the candidate's actual experience and projects.\n"
                +
                "Your response must be a JSON array of strings, where each string is a single question.\n" +
                "Candidate Resume:\n" + resumeText;

        String rawOutput;
        try {
            rawOutput = chatModel.call(prompt);
        } catch (Exception e) {
            throw new AiServiceException("Failed to generate questions using AI model", e);
        }

        String cleanJson = rawOutput.trim();
        if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(cleanJson.indexOf('\n') + 1, cleanJson.lastIndexOf("```")).trim();
        }

        List<String> questionsList = new ArrayList<>();
        try {
            String[] questionsArray = new com.fasterxml.jackson.databind.ObjectMapper().readValue(cleanJson,
                    String[].class);
            questionsList = Arrays.asList(questionsArray);
        } catch (Exception e) {
            questionsList = parseQuestionsFallback(rawOutput);
        }

        if (questionsList.isEmpty()) {
            questionsList = Arrays.asList(
                    "Can you walk me through one of the key projects listed in your resume?",
                    "What technical challenges did you face in your projects and how did you resolve them?",
                    "How do you handle scalability and performance optimization in your applications?",
                    "Which programming languages and frameworks are you most comfortable with, and why?",
                    "Can you explain your experience with database design and query optimization?");
        }

        List<Question> questions = new ArrayList<>();
        int index = 0;
        for (String qText : questionsList) {
            Question question = Question.builder()
                    .prompt(qText)
                    .orderIndex(index++)
                    .session(savedSession)
                    .build();
            questions.add(questionRepository.save(question));
        }

        savedSession.setQuestions(questions);
        savedSession.setGeneratedQuestions(String.join("\n", questionsList));
        return sessionRepository.save(savedSession);
    }

    /**
     * Helper to retrieve all questions for a specific session.
     */
    public List<Question> getQuestionsForSession(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found");
        }
        return questionRepository.findBySessionIdOrderByOrderIndexAsc(sessionId);
    }

    /**
     * Legacy method: transcribes audio and evaluates in one block.
     */
    public String transcribeAudioAndEvaluate(Long sessionId, MultipartFile audioFile) {
        String transcript = transcribeWithWhisper(audioFile);

        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.setUserAnswerTranscript(transcript);

        String feedbackPrompt = "You are an expert technical interviewer. The candidate was asked these questions:\n"
                + session.getGeneratedQuestions() + "\n\n"
                + "The candidate provided the following spoken answer:\n" + transcript + "\n\n"
                + "Provide constructive feedback on their answer. What did they do well? What could be improved? Give them a score out of 10.";

        String feedback;
        try {
            feedback = chatModel.call(feedbackPrompt);
        } catch (Exception e) {
            throw new AiServiceException("Failed to evaluate candidate feedback using AI", e);
        }

        session.setAiFeedback(feedback);
        sessionRepository.save(session);
        return feedback;
    }

    private String transcribeWithWhisper(MultipartFile audioFile) {
        try {
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
                    java.util.Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("text");
            } else {
                throw new TranscriptionException("Whisper response status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new TranscriptionException("Failed to transcribe audio via Whisper API", e);
        }
    }

    private List<String> parseQuestionsFallback(String rawOutput) {
        List<String> list = new ArrayList<>();
        if (rawOutput == null)
            return list;
        String[] lines = rawOutput.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.matches("^\\d+\\..*")) {
                list.add(trimmed.replaceFirst("^\\d+\\.\\s*", "").trim());
            } else if (trimmed.startsWith("-") || trimmed.startsWith("*")) {
                list.add(trimmed.substring(1).trim());
            }
        }
        return list;
    }

    private double parseScore(String evaluationResult) {
        try {
            int scoreIndex = evaluationResult.toLowerCase().indexOf("score:");
            if (scoreIndex != -1) {
                int endOfLine = evaluationResult.indexOf('\n', scoreIndex);
                String scoreStr;
                if (endOfLine != -1) {
                    scoreStr = evaluationResult.substring(scoreIndex + 6, endOfLine).trim();
                } else {
                    scoreStr = evaluationResult.substring(scoreIndex + 6).trim();
                }
                scoreStr = scoreStr.replaceAll("[^0-9.]", "");
                return Double.parseDouble(scoreStr);
            }
        } catch (Exception e) {
            System.err.println("Error parsing score: " + e.getMessage());
        }
        return 5.0;
    }

    private String parseFeedback(String evaluationResult) {
        int feedbackIndex = evaluationResult.toLowerCase().indexOf("feedback:");
        if (feedbackIndex != -1) {
            return evaluationResult.substring(feedbackIndex + 9).trim();
        }
        return evaluationResult.trim();
    }
}
