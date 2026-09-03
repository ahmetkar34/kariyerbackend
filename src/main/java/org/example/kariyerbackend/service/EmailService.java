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

    public void sendVerificationEmail(String to, String code) {
        send(to, "E-posta Doğrulama Kodunuz", """
                <p>KariyerBul'a hoş geldiniz!</p>
                <p>Hesabınızı doğrulamak için aşağıdaki kodu girin:</p>
                <p style="font-size:28px;font-weight:bold;letter-spacing:4px;">%s</p>
                <p>Bu kod 15 dakika geçerlidir.</p>
                """.formatted(code));
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
