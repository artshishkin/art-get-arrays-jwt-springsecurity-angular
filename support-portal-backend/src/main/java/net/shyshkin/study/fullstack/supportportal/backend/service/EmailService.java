package net.shyshkin.study.fullstack.supportportal.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

import static net.shyshkin.study.fullstack.supportportal.backend.constant.EmailConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final Environment environment;

    public void sendNewPasswordEmail(String firstName, String password, String email) throws MessagingException {
        Message message = createEmail(firstName, password, email);
        Transport transport = getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL);

        String mailUsername = environment.getProperty("PORTAL_MAIL_USERNAME", USERNAME);
        String mailPassword = environment.getProperty("PORTAL_MAIL_PASSWORD", PASSWORD);

        transport.connect(GMAIL_SMTP_SERVER, mailUsername, mailPassword);
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();
    }

    private Message createEmail(String firstName, String password, String email) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
        message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(CC_EMAIL, false));
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello " + firstName + "!\n\nYour new account password is: " + password + "\n\nThe Support Team");
        message.setSentDate(new Date());
        message.saveChanges();
        return message;
    }

    private Session getEmailSession() {

        Properties properties = System.getProperties();

        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);

        return Session.getInstance(properties);
    }
}
