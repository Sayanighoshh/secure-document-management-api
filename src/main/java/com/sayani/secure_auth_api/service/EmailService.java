package com.sayani.secure_auth_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendRegistrationEmail(String toEmail, String username, String rawPassword) {
        String subject = "Welcome to Secure Auth API - Account Registration";
        String messageBody = String.format(
                "Hello %s,\n\n" +
                "Your account has been successfully registered!\n\n" +
                "Your Account Details:\n" +
                "------------------------------------\n" +
                "Username / Email: %s\n" +
                "Password: %s\n" +
                "------------------------------------\n\n" +
                "Two-Factor Authentication is enabled on your account.\n" +
                "Whenever a new or unrecognized device attempts to log in, an OTP will be sent to this email.\n\n" +
                "Best regards,\n" +
                "Secure Auth Team",
                username, toEmail, rawPassword
        );

        sendEmail(toEmail, subject, messageBody);
    }

    public void sendOtpEmail(String toEmail, String username, String otpCode, String deviceId) {
        String subject = "Two-Factor Authentication (OTP) Security Alert";
        String messageBody = String.format(
                "Hello %s,\n\n" +
                "A login attempt was detected from a new or unrecognized device: [%s].\n\n" +
                "Your One-Time Password (OTP) for Two-Factor Authentication is:\n\n" +
                "        >>>  %s  <<<\n\n" +
                "This OTP is valid for 5 minutes. Do not share this code with anyone.\n" +
                "If you did not initiate this login request, please secure your account immediately.\n\n" +
                "Best regards,\n" +
                "Secure Auth Security Team",
                username, deviceId, otpCode
        );

        sendEmail(toEmail, subject, messageBody);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        // Always log to console for easy developer testing and visibility
        log.info("\n" +
                "================================================================================\n" +
                "[SECURE EMAIL SERVICE - NOTIFICATION DISPATCHED]\n" +
                "To: {}\n" +
                "Subject: {}\n" +
                "--------------------------------------------------------------------------------\n" +
                "{}\n" +
                "================================================================================",
                toEmail, subject, body);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Email successfully sent via SMTP to: {}", toEmail);
            } catch (Exception ex) {
                log.warn("SMTP host not connected or not configured. (Console fallback active). Details: {}", ex.getMessage());
            }
        } else {
            log.info("JavaMailSender bean not configured. Delivered via Console Logger.");
        }
    }
}
