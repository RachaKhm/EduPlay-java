package dev.eduplay.config;

import java.util.Properties;

public class EmailConfig {

    // ⚠️ À MODIFIER AVEC VOS IDENTIFIANTS
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USERNAME = "rachakhamassi03@gmail.com";  // ← À MODIFIER
    private static final String SMTP_PASSWORD = "sjamvfzwpsnbehcq";  // ← À MODIFIER
    private static final String FROM_EMAIL = "noreply@eduplay.com";
    private static final String FROM_NAME = "EduPlay - Plateforme Éducative";

    public static Properties getSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        props.put("mail.debug", "true");
        return props;
    }

    public static String getUsername() {
        return SMTP_USERNAME;
    }

    public static String getPassword() {
        return SMTP_PASSWORD;
    }

    public static String getFromEmail() {
        return FROM_EMAIL;
    }

    public static String getFromName() {
        return FROM_NAME;
    }
}