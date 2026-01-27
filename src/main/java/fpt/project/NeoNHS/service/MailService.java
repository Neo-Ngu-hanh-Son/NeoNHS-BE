package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.constants.EmailTemplate;
import fpt.project.NeoNHS.entity.User;

public interface MailService {
    void sendVerifyEmailAsync(User user, EmailTemplate template);
}
