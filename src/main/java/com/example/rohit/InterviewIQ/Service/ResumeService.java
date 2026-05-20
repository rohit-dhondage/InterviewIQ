package com.example.rohit.InterviewIQ.Service;

import com.example.rohit.InterviewIQ.DTO.ResumeUploadResponse;
import com.example.rohit.InterviewIQ.Exception.InvalidFileFormatException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeService {

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
     * @param file  The uploaded PDF resume
     * @return      ResumeUploadResponse containing extracted text and metadata
     */
    public ResumeUploadResponse processResume(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded. Please attach a PDF resume.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are accepted. Please upload a .pdf file.");
        }

        // File size guard: reject files larger than 5MB to prevent memory issues
        long maxSizeBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("File is too large. Maximum allowed size is 5MB.");
        }

        // PDF Text Extraction
        String extractedText;
        try {
            extractedText = pdfManager.extractText(file);
        } catch (IOException e) {
            throw new InvalidFileFormatException("Failed to parse PDF resume: " + e.getMessage(), e);
        }

        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "No text could be extracted from this PDF. " +
                "If your resume is a scanned image, please use a text-based PDF."
            );
        }

        return new ResumeUploadResponse(
            extractedText,
            "Resume uploaded and text extracted successfully. Ready for AI analysis."
        );
    }
}
