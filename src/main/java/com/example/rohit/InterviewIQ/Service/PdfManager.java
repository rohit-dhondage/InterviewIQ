package com.example.rohit.InterviewIQ.Service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// @Service tells Spring: "Hey, manage this class as a Spring Bean."
// This means we can inject PdfManager into any other class using @Autowired or constructor injection.
@Service
public class PdfManager {

    /**
     * Extracts plain text from a PDF file uploaded by the user.
     *
     * Real-world note: Resumes come as PDFs. We need raw text to send to AI for analysis.
     * Apache PDFBox handles the heavy lifting of parsing binary PDF format → readable String.
     *
     * @param file  The uploaded PDF file (from HTTP multipart request)
     * @return      The full extracted text content of the PDF
     * @throws IOException  If the file is corrupt, unreadable, or not a valid PDF
     */
    public String extractText(MultipartFile file) throws IOException {

        // Loader.loadPDF() reads the raw bytes of the uploaded file and parses the PDF structure
        // PDDocument is PDFBox's representation of an open PDF — always close it after use (try-with-resources handles this)
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {

            // PDFTextStripper walks through every page of the PDF and extracts all text
            PDFTextStripper stripper = new PDFTextStripper();

            // getText() returns the entire text content as a single String
            return stripper.getText(document);

        }
        // try-with-resources automatically calls document.close() even if an exception occurs
        // This prevents memory leaks — very important in production apps
    }
}

