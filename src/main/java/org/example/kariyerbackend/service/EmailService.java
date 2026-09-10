package org.example.kariyerbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.kariyerbackend.entity.ApplicationStatus;
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

    public void sendApplicationStatusEmail(
            String to, String candidateFirstName, String jobTitle, String companyName, ApplicationStatus status
    ) {
        if (status == ApplicationStatus.ACCEPTED) {
            send(to, "Başvurunuz kabul edildi: " + jobTitle, """
                    <p>Merhaba %s,</p>
                    <p><strong>%s</strong> firmasındaki <strong>%s</strong> pozisyonuna yaptığınız başvuru kabul edildi. Tebrikler!</p>
                    <p>Detaylar için <a href="%s/basvurularim">başvurularım</a> sayfasını ziyaret edebilirsiniz.</p>
                    """.formatted(candidateFirstName, companyName, jobTitle, frontendUrl));
        } else if (status == ApplicationStatus.REJECTED) {
            send(to, "Başvurunuz hakkında güncelleme: " + jobTitle, """
                    <p>Merhaba %s,</p>
                    <p><strong>%s</strong> firmasındaki <strong>%s</strong> pozisyonuna yaptığınız başvuru bu kez olumlu sonuçlanmadı.</p>
                    <p>Sizin için uygun diğer ilanlara <a href="%s">KariyerBul</a> üzerinden göz atabilirsiniz.</p>
                    """.formatted(candidateFirstName, companyName, jobTitle, frontendUrl));
        }
    }

    public void sendJobAlertEmail(String to, String candidateFirstName, String jobTitle, String companyName, Long jobId) {
        String link = frontendUrl + "/ilan/" + jobId;
        send(to, "Aradığınız kriterlere uygun yeni bir ilan: " + jobTitle, """
                <p>Merhaba %s,</p>
                <p>Kaydettiğiniz bir iş ilanı uyarısına uyan yeni bir ilan yayınlandı:</p>
                <p><strong>%s</strong> - %s</p>
                <p><a href="%s">İlanı görüntülemek için tıklayın</a>.</p>
                """.formatted(candidateFirstName, jobTitle, companyName, link));
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
