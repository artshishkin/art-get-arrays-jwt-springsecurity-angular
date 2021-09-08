package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.EmailConstant.EMAIL_SUBJECT;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final Environment environment;

    public void sendNewPasswordEmail(String firstName, String password, String email) {

        // Create a Simple MailMessage.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        String carbonCopyEmail = environment.getProperty("spring.mail.username");
        log.debug("Carbon Copy Email: {}", carbonCopyEmail);
        message.setCc(carbonCopyEmail);
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello " + firstName + "!\n\nYour new account password is: " + password + "\n\nThe Support Team");

        // Send Message!
        this.emailSender.send(message);
    }
}
