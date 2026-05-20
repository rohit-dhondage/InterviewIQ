package com.example.rohit.InterviewIQ.Controller;

import com.example.rohit.InterviewIQ.DTO.ResumeUploadResponse;
import com.example.rohit.InterviewIQ.Service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * POST /resume/upload
     * Accepts a PDF resume file from the client and returns the extracted text.
     *
     * @param file  The uploaded resume PDF
     * @return      200 OK with extracted text, or error message (handled globally)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) {
        ResumeUploadResponse response = resumeService.processResume(file);
        return ResponseEntity.ok(response);
    }
}
