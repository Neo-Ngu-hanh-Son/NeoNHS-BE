package fpt.project.NeoNHS.service;

import java.util.Map;

public interface TranslationService {
    /**
     * Translate a single text
     */
    String translate(String text, String targetLang);

    /**
     * Translate multiple fields at once
     */
    Map<String, String> translateFields(Map<String, String> fields, String targetLang);
}
