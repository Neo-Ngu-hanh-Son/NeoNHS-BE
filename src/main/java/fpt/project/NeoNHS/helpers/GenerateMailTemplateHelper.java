package fpt.project.NeoNHS.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GenerateMailTemplateHelper {
    private final TemplateEngine templateEngine;

    public String generateVerificationEmail(String email, String verificationCode, String template) {
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verificationCode", verificationCode);
        return templateEngine.process(template, context);
    }
}
