package fpt.project.NeoNHS.constants;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    VERIFY_ACCOUNT("verification_email", "Your verification code for JLearning"),
    RESET_PASSWORD("reset_password_email", "Reset password for JLearning"),
    USER_APPOINTMENT_REMINDER("appointment_reminder_email", "Reminder upcoming appointment for JLearning"),
    LECTURER_APPOINTMENT_REMINDER("lecture_reminder_email", "Reminder upcoming appointment for JLearning"),
    ;

    private final String templateFile;
    private final String subject;

    EmailTemplate(String templateFile, String subject) {
        this.templateFile = templateFile;
        this.subject = subject;
    }
}
