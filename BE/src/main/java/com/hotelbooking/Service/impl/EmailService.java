package com.hotelbooking.Service.impl;

import com.hotelbooking.Entities.UserEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendApprovedMail(UserEntity user) {
        String subject = "Yêu cầu trở thành Quản lý khách sạn đã được phê duyệt";
        String content = """
                <p>Xin chào <b>%s</b>,</p>
                <p>Chúc mừng! Yêu cầu trở thành <b>Quản lý khách sạn</b> của bạn đã được phê duyệt.</p>
                <p>Bây giờ bạn có thể đăng nhập và bắt đầu đăng khách sạn của mình, vui lòng đăng nhập lại để sử dụng.</p>
                <p>Trân trọng,<br>Hotel Booking Team</p>
                """.formatted(user.getFullName());

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);   // true = HTML
            mailSender.send(msg);
        } catch (MessagingException e) {
            // log lỗi, KHÔNG throw để không rollback transaction
            System.err.println("Gửi mail thất bại: " + e.getMessage());
        }
    }

    @Async
    public void sendRejectedMail(UserEntity user, String note) {
        String subject = "Yêu cầu trở thành Quản lý khách sạn bị từ chối";
        String content = """
                <p>Xin chào <b>%s</b>,</p>
                <p>Rất tiếc! Yêu cầu trở thành Quản lý khách sạn của bạn đã bị từ chối.</p>
                <p>Lý do: <i>%s</i></p>
                <p>Nếu có thắc mắc, hãy liên hệ đội ngũ hỗ trợ.</p>
                <p>Trân trọng,<br>Hotel Booking Team</p>
                """.formatted(user.getFullName(), note == null ? "Không có" : note);

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(msg);
        } catch (MessagingException e) {
            System.err.println("Gửi mail thất bại: " + e.getMessage());
        }
    }
}
