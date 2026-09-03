package org.example.kariyerbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public void sendVerificationEmail(String to, String token) {
        String link = frontendUrl + "/dogrula?token=" + token;
        send(to, "E-posta Adresinizi Doğrulayın", """
                <p>KariyerBul'a hoş geldiniz!</p>
                <p>Hesabınızı doğrulamak için <a href="%s">buraya tıklayın</a>.</p>
                <p>Bu bağlantı 24 saat geçerlidir.</p>
                """.formatted(link));
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = frontendUrl + "/sifre-sifirla?token=" + token;
        send(to, "Şifre Sıfırlama", """
                <p>Şifrenizi sıfırlamak için <a href="%s">buraya tıklayın</a>.</p>
                <p>Bu bağlantı 1 saat geçerlidir. Bu isteği siz yapmadıysanız bu e-postayı yok sayabilirsiniz.</p>
                """.formatted(link));
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("E-posta gönderilemedi", ex);
        }
    }
}
