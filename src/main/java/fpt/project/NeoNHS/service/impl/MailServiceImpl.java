package fpt.project.NeoNHS.service.impl;


import fpt.project.NeoNHS.constants.EmailTemplate;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.exception.EmailException;
import fpt.project.NeoNHS.helpers.GenerateMailTemplateHelper;
import fpt.project.NeoNHS.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender emailSender;
    private final GenerateMailTemplateHelper mailTemplateHelper;

    @Override
    @Async
    public void sendVerifyEmailAsync(User user, EmailTemplate template, String code, String appUrl) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject(template.getSubject());
        helper.setText(mailTemplateHelper
            .generateVerificationEmail(user.getFullname(), code, template.getTemplateFile(), appUrl), true);
        } catch (MessagingException e) {
            throw new EmailException("Failed to send email: " + e.getMessage());
        }
        emailSender.send(message);
    }
}
