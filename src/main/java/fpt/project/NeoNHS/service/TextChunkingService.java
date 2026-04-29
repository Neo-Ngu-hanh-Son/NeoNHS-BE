package fpt.project.NeoNHS.service;

import java.util.List;

/**
 * Service for splitting long text documents into smaller, overlapping chunks.
 * This improves vector search quality by ensuring each chunk has a focused semantic meaning.
 */
public interface TextChunkingService {

    /**
     * Split text into chunks with overlap.
     * Uses smart boundary detection: paragraph breaks → sentence boundaries → word boundaries.
     *
     * @param text      the full text to chunk
     * @param chunkSize maximum characters per chunk
     * @param overlap   number of overlapping characters between adjacent chunks
     * @return list of text chunks
     */
    List<String> chunkText(String text, int chunkSize, int overlap);

    /**
     * Split text using default chunk size (600) and overlap (100).
     *
     * @param text the full text to chunk
     * @return list of text chunks
     */
    List<String> chunkText(String text);
}
