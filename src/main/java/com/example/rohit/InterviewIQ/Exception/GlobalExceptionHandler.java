package com.example.rohit.InterviewIQ.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TranscriptionException.class)
    public ResponseEntity<String> handleTranscriptionException(TranscriptionException ex) {
        return new ResponseEntity<>("Audio Transcription Error: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<String> handleAiServiceException(AiServiceException ex) {
        return new ResponseEntity<>("AI Model Service Error: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<String> handleInvalidFileFormatException(InvalidFileFormatException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        ex.printStackTrace(); // Optional: log it using a proper logger in real prod
        return new ResponseEntity<>("An internal server error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
