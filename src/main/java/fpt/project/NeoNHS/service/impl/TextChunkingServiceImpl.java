package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.service.TextChunkingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Smart text chunking implementation that splits text at natural boundaries.
 * Priority: paragraph breaks → sentence boundaries → word boundaries.
 * Supports overlapping chunks for better vector search continuity.
 */
@Service
public class TextChunkingServiceImpl implements TextChunkingService {

    @Value("${vector-search.chunk-size:600}")
    private int defaultChunkSize;

    @Value("${vector-search.chunk-overlap:100}")
    private int defaultOverlap;

    @Override
    public List<String> chunkText(String text) {
        return chunkText(text, defaultChunkSize, defaultOverlap);
    }

    @Override
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        // If text fits in a single chunk, no need to split
        String trimmed = text.trim();
        if (trimmed.length() <= chunkSize) {
            return List.of(trimmed);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < trimmed.length()) {
            int end = Math.min(start + chunkSize, trimmed.length());

            if (end < trimmed.length()) {
                // Try to find a natural break point within the chunk
                end = findBestBreakPoint(trimmed, start, end);
            }

            String chunk = trimmed.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // Move start forward, accounting for overlap
            int step = end - start - overlap;
            if (step <= 0) {
                // Prevent infinite loop: force forward progress
                step = Math.max(1, chunkSize / 2);
            }
            start += step;
        }

        return chunks;
    }

    /**
     * Find the best break point near the end of a chunk.
     * Tries paragraph boundary first, then sentence boundary, then word boundary.
     */
    private int findBestBreakPoint(String text, int start, int maxEnd) {
        // Search window: look back up to 30% of chunk size for a good break
        int searchStart = Math.max(start, maxEnd - (maxEnd - start) / 3);

        // Priority 1: Paragraph break (\n\n)
        int paragraphBreak = text.lastIndexOf("\n\n", maxEnd);
        if (paragraphBreak > searchStart) {
            return paragraphBreak + 2; // Include the newlines in current chunk
        }

        // Priority 2: Single line break (\n)
        int lineBreak = text.lastIndexOf("\n", maxEnd);
        if (lineBreak > searchStart) {
            return lineBreak + 1;
        }

        // Priority 3: Sentence boundary (. or ! or ? followed by space)
        for (int i = maxEnd - 1; i > searchStart; i--) {
            char c = text.charAt(i);
            if ((c == '.' || c == '!' || c == '?') && i + 1 < text.length() && text.charAt(i + 1) == ' ') {
                return i + 1; // Break after the punctuation
            }
        }

        // Priority 4: Word boundary (space)
        int lastSpace = text.lastIndexOf(' ', maxEnd);
        if (lastSpace > searchStart) {
            return lastSpace + 1;
        }

        // Fallback: hard cut at maxEnd
        return maxEnd;
    }
}
