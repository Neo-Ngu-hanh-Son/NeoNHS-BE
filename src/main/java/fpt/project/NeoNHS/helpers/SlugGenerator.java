package fpt.project.NeoNHS.helpers;

import java.text.Normalizer;

public class SlugGenerator {
  public static String generateSlug(String name) {
    if (name == null)
      return null;

    String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);

    // Remove accents (dấu)
    String noAccent = normalized.replaceAll("\\p{M}", "");

    // Handle special Vietnamese letter Đ/đ
    noAccent = noAccent.replace("đ", "d").replace("Đ", "D");

    return noAccent
        .toLowerCase()
        .trim()
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", ""); // remove leading/trailing '-'
  }
}
