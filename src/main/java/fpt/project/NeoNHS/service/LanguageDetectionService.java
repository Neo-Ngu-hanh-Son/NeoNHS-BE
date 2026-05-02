package fpt.project.NeoNHS.service;

/**
 * Service responsible for detecting the language of a user message
 * and normalising it to Vietnamese so that the Vector Search index
 * (which is stored entirely in Vietnamese) can return relevant results.
 */
public interface LanguageDetectionService {

    /**
     * Detect the ISO 639-1 language code of the given text.
     * Example return values: "vi", "en", "ja", "ko", "zh", "fr", "de", ...
     *
     * @param text the user message
     * @return ISO 639-1 language code, or "vi" as fallback
     */
    String detectLanguage(String text);

    /**
     * If the text is already Vietnamese, return it unchanged.
     * Otherwise translate it to Vietnamese using GPT-4o-mini.
     *
     * @param text         the user message (any language)
     * @param detectedLang the ISO 639-1 code returned by {@link #detectLanguage}
     * @return the Vietnamese equivalent of the text, suitable for vector search
     */
    String translateToVietnamese(String text, String detectedLang);
}
