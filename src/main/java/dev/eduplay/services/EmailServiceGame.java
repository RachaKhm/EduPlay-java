package dev.eduplay.services;

import dev.eduplay.tools.MyDataBase;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailServiceGame {

    private Connection cnx;

    public EmailServiceGame() throws SQLException {
        cnx = MyDataBase.getInstance().getCnx();
    }

    // Récupérer tous les emails des parents
    public List<String> getAllParentEmails() {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM user WHERE type = 'parent'";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                emails.add(rs.getString("email"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emails;
    }

    // Configuration SMTP
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_FROM = "eduplayeduplay4@gmail.com";
    private static final String EMAIL_PASSWORD = "lnbc xksy pyjx cvod";

    // Envoyer email à tous les parents
    public void sendEmailToAllParents(String gameName, String gameDescription) {
        List<String> parentEmails = getAllParentEmails();

        if (parentEmails.isEmpty()) {
            System.out.println("Aucun parent trouvé dans la base de données");
            return;
        }

        for (String email : parentEmails) {
            sendEmail(email, gameName, gameDescription);
        }

        System.out.println("Email envoyé à " + parentEmails.size() + " parents");
    }

    // Envoyer un seul email
    private void sendEmail(String toEmail, String gameName, String gameDescription) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🎮 Nouveau jeu éducatif disponible sur EduPlay !");
            message.setContent(buildEmailContent(gameName, gameDescription), "text/html");

            Transport.send(message);
            System.out.println("Email envoyé à : " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Erreur d'envoi à " + toEmail + " : " + e.getMessage());
        }
    }

    // Contenu de l'email en HTML
    private String buildEmailContent(String gameName, String gameDescription) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<div style='background-color: #1A1A2E; padding: 20px; text-align: center;'>" +
                "<h2 style='color: #E94560;'>🎮 EduPlay</h2>" +
                "</div>" +
                "<div style='padding: 20px;'>" +
                "<h3>✨ Nouveau jeu disponible !</h3>" +
                "<p>Votre enfant peut découvrir un nouveau jeu éducatif :</p>" +
                "<p><strong>📝 Nom du jeu :</strong> " + gameName + "</p>" +
                "<p><strong>📖 Description :</strong> " + gameDescription + "</p>" +
                "<br>" +
                "<p>🔗 Connectez-vous à EduPlay pour que votre enfant puisse jouer !</p>" +
                "<a href='http://localhost:8080/login' style='background-color: #E94560; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>🚀 Accéder à EduPlay</a>" +
                "</div>" +
                "<div style='background-color: #f8f9fa; padding: 10px; text-align: center; font-size: 12px; color: #777;'>" +
                "<p>© 2025 EduPlay - Apprentissage ludique</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}