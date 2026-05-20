package com.example.rohit.InterviewIQ.Exception;

public class TranscriptionException extends RuntimeException {
    public TranscriptionException(String message) {
        super(message);
    }

    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
