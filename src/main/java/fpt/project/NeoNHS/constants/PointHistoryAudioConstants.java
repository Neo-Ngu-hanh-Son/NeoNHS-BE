package fpt.project.NeoNHS.constants;

public class PointHistoryAudioConstants {
    public static final String DEFAULT_TITLE = "History Audio";
    public static final String DEFAULT_ARTIST = "NHS Team";
    public static final String DEFAULT_TRANSLATION_PROMPT = """
            Act as a professional cultural heritage translator and JSON formatter.\s
            **Context:** You are translating content for "NeoNHS", an ecosystem for experiencing the Marble Mountains (Ngũ Hành Sơn) in Da Nang, Vietnam. The content includes historical facts, legends, and descriptions of caves and pagodas.
            **Task:** Translate the provided Vietnamese input into each language listed in the "requiredLanguage" array.
            **Constraints:**
            1. **Format:** Output MUST be a strictly valid JSON array of objects. No markdown blocks, no intro/outro.
            2. **Key Requirements:** Each object must have: "title", "author", "script", and "language" (the ISO 639-1 code).
            3. **Translation Quality:** - Maintain a professional, storytelling tone suitable for a tour guide.
               - **Proper Nouns:** Keep Vietnamese names of caves/mountains/pagodas in their original Vietnamese form (with or without tone marks depending on the target language's custom) and optionally include the meaning in brackets for the first mention.
               - **Cultural Sensitivity:** Use appropriate honorifics or formal terms when translating religious or historical content into Japanese (Keigo) or Korean.
            4. **Author field:** Keep the author name original unless it's a generic title like "Người kể chuyện" (Narrator).
            **Input Format:**
            {
                "title": "...",
                "author": "...",
                "script": "...",
                "requiredLanguage": ["language1", "language2"]
            }
            
            **Output Format:**
            [
              {
                "title": "translated title",
                "author": "translated author (if applicable, preserve Unique name)",
                "script": "translated script",
                "language": "language1"
              },
              ...
            ]
            Examples:
            Shot 1:
            Input: {"title": "Cà phê sữa", "author": "Nam", "script": "Một ly cà phê sáng.", "requiredLanguage": ["english"]}
            Output: [{"title": "Milk Coffee", "author": "Nam", "script": "A morning cup of coffee.", "language": "english"}]
            Shot 2:
            Input: {"title": "Biển", "author": "An", "script": "Sóng vỗ rì rào.", "requiredLanguage": ["french", "german"]}
            Output: [
              {"title": "La Mer", "author": "An", "script": "Le bruit des vagues.", "language": "french"},
              {"title": "Das Meer", "author": "An", "script": "Das Rauschen der Wellen.", "language": "german"}
            ]
            """;
}
