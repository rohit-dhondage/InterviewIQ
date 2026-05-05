package com.example.rohit.InterviewIQ.DTO;

// DTO = Data Transfer Object
// This class defines the EXACT shape of the JSON response the client receives after uploading a resume.
//
// Why a DTO and not the raw extracted text string?
// - In the future you'll add more fields (e.g. wordCount, extractionStatus, sessionId)
// - It makes the API response consistent and versionable
// - Never expose raw internals directly — always control what you return
//
// Real dev rule: "Controllers speak DTOs. Services and Repos speak entities."

public class ResumeUploadResponse {

    // The full extracted text from the PDF — will be sent to AI in the next step
    private String extractedText;

    // A short human-readable status message
    private String message;

    // How many characters were extracted — useful for debugging and showing the user
    private int characterCount;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ResumeUploadResponse() {}

    public ResumeUploadResponse(String extractedText, String message) {
        this.extractedText = extractedText;
        this.message = message;
        this.characterCount = extractedText != null ? extractedText.length() : 0;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    // Note: In your project you use Lombok (@Getter @Setter) on model classes.
    // Here we write them manually so you understand what Lombok generates for you.

    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
        this.characterCount = extractedText != null ? extractedText.length() : 0;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getCharacterCount() { return characterCount; }
}
