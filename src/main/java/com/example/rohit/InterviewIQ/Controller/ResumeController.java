package com.example.rohit.InterviewIQ.Controller;

import com.example.rohit.InterviewIQ.DTO.ResumeUploadResponse;
import com.example.rohit.InterviewIQ.Service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// @RestController = @Controller + @ResponseBody
// It means: every method return value is automatically serialized to JSON and written to the HTTP response body.
//
// @RequestMapping("/resume") sets the BASE URL path for all endpoints in this controller.
// So all endpoints here start with /resume/...
//
// Security: This controller is PROTECTED by JWT because SecurityConfig says:
//   .anyRequest().authenticated()
// Any request to /resume/** will require a valid Bearer token in the Authorization header.
@RestController
@RequestMapping("/resume")
public class ResumeController {

    // The controller DELEGATES all logic to the service — it never does business logic itself.
    // This keeps the controller thin and testable.
    private final ResumeService resumeService;

    // Constructor injection (preferred in companies over @Autowired)
    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    /**
     * POST /resume/upload
     *
     * Accepts a PDF resume file from the client and returns the extracted text.
     *
     * How to call this endpoint (e.g. from Postman or frontend):
     *   - Method: POST
     *   - URL: http://localhost:8080/resume/upload
     *   - Headers: Authorization: Bearer <your_jwt_token>
     *   - Body: form-data → key="file", type=File, value=<your_resume.pdf>
     *
     * @param file  The uploaded resume PDF — comes from multipart/form-data request
     * @return      200 OK with extracted text, or 400 Bad Request with error message
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            // @RequestParam("file") maps the "file" field from the multipart form to this parameter
            // MultipartFile is Spring's abstraction for an uploaded file
            @RequestParam("file") MultipartFile file) {

        try {
            // Delegate to service — controller just passes data and returns the result
            ResumeUploadResponse response = resumeService.processResume(file);

            // ResponseEntity.ok() = HTTP 200 OK + body as JSON
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // IllegalArgumentException = client sent bad data (wrong file type, empty file, etc.)
            // We return 400 Bad Request with the error message so the client knows what they did wrong
            // .badRequest() = HTTP 400
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IOException e) {
            // IOException = something went wrong reading the file on the server side
            // This is a SERVER error, so we return 500 Internal Server Error
            // We do NOT expose raw exception messages to clients in production (security risk)
            // In production you'd log this with a proper logger (SLF4J/Logback)
            System.err.println("Error reading PDF: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the uploaded file. Please try again.");
        }
    }
}
