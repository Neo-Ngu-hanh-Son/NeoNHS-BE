package fpt.project.NeoNHS.constants;

import lombok.Getter;

@Getter
public enum EmailTemplate {
    VERIFY_ACCOUNT("verification_email", "Your verification code for CCTE"),
    RESET_PASSWORD("reset_password_email", "Reset password for CCTE"),
    USER_APPOINTMENT_REMINDER("appointment_reminder_email", "Reminder upcoming appointment for CCTE"),
    LECTURER_APPOINTMENT_REMINDER("lecture_reminder_email", "Reminder upcoming appointment for CCTE"),
    SET_PASSWORD("set_password_email", "Set password for your Ngu Hanh Son account"),
    ;

    private final String templateFile;
    private final String subject;

    EmailTemplate(String templateFile, String subject) {
        this.templateFile = templateFile;
        this.subject = subject;
    }
}
