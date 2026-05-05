package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.DTO.ResumeUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// This Service class contains all the BUSINESS LOGIC for resume processing.
//
// Real-world architecture rule:
//   Controller  →  handles HTTP (request/response, status codes)
//   Service     →  handles BUSINESS LOGIC (what to do with the data)
//   Repository  →  handles DATABASE (save, find, delete)
//
// Think of it like a restaurant:
//   Controller = Waiter (takes your order, gives you the food)
//   Service    = Chef   (actually makes the food)
//   Repository = Fridge (stores ingredients)

@Service
public class ResumeService {

    // Constructor injection — Spring automatically provides PdfManager because it's @Service
    // This is preferred over @Autowired field injection in professional codebases
    // because it makes dependencies explicit and makes unit testing easier
    private final PdfManager pdfManager;

    public ResumeService(PdfManager pdfManager) {
        this.pdfManager = pdfManager;
    }

    /**
     * Handles the full resume upload flow:
     * 1. Validates the uploaded file
     * 2. Extracts text using PDFBox
     * 3. Returns structured response
     *
     * Later, Step 3 will add: send extracted text to AI for analysis.
     *
     * @param file  The uploaded PDF resume
     * @return      ResumeUploadResponse containing extracted text and metadata
     * @throws IOException  If file reading fails
     */
    public ResumeUploadResponse processResume(MultipartFile file) throws IOException {

        // ── Validation ────────────────────────────────────────────────────────
        // Real devs ALWAYS validate input before processing.
        // "Never trust client input" is a core security principle.

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded. Please attach a PDF resume.");
        }

        // Check that the uploaded file is actually a PDF
        // getOriginalFilename() returns the filename the client sent (e.g. "resume.pdf")
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are accepted. Please upload a .pdf file.");
        }

        // File size guard: reject files larger than 5MB to prevent memory issues
        // 5 * 1024 * 1024 = 5,242,880 bytes = 5MB
        long maxSizeBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File is too large. Maximum allowed size is 5MB.");
        }

        // ── PDF Text Extraction ───────────────────────────────────────────────
        // Delegate to PdfManager — it knows HOW to extract text (Apache PDFBox)
        // This service doesn't care about PDFBox internals, just the result
        String extractedText = pdfManager.extractText(file);

        // Edge case: PDF parsed successfully but contained no text
        // This can happen with scanned image PDFs (they need OCR, not text extraction)
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "No text could be extracted from this PDF. " +
                "If your resume is a scanned image, please use a text-based PDF."
            );
        }

        // ── Build Response ────────────────────────────────────────────────────
        return new ResumeUploadResponse(
            extractedText,
            "Resume uploaded and text extracted successfully. Ready for AI analysis."
        );
    }
}
