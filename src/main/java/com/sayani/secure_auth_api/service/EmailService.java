package com.sayani.secure_auth_api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Dispatches welcome registration email with auto-generated credentials.
     */
    public void sendRegistrationEmail(String toEmail, String fullName, String password) {
        String subject = "Welcome to Secure Auth - Account Registration Details";
        String messageBody = String.format(
                "Dear %s,\n\n" +
                "Welcome to Secure Auth API! Your account registration has been completed successfully.\n\n" +
                "Here are your account credentials:\n" +
                "--------------------------------------------------\n" +
                "Registered Email / Username: %s\n" +
                "Auto-Generated Password:     %s\n" +
                "--------------------------------------------------\n\n" +
                "Security Recommendations:\n" +
                "1. For enhanced account security, you may update your password at any time via the 'Forgot Password' feature.\n" +
                "2. Two-Factor Authentication (2FA) is automatically enabled on your account.\n" +
                "3. If you log in from an unrecognized device, a secure OTP verification will be required.\n\n" +
                "Best regards,\n" +
                "Secure Auth Security Team",
                fullName, toEmail, password
        );

        sendEmail(toEmail, subject, messageBody);
    }

    /**
     * Dispatches OTP for Forgot Password / Reset Password request.
     */
    public void sendPasswordResetOtpEmail(String toEmail, String fullName, String otpCode) {
        String subject = "Password Reset Request - Verification OTP";
        String messageBody = String.format(
                "Dear %s,\n\n" +
                "We received a request to reset the password for your Secure Auth account (%s).\n\n" +
                "Your One-Time Password (OTP) for password reset is:\n\n" +
                "        ================================\n" +
                "               >>>  %s  <<<\n" +
                "        ================================\n\n" +
                "This OTP is valid for 5 minutes. Enter this code to verify your identity and set a new password.\n\n" +
                "SECURITY NOTICE:\n" +
                "If you did NOT request a password reset, please ignore this email or contact support immediately. Your password remains unchanged until verified.\n\n" +
                "Best regards,\n" +
                "Secure Auth Security Team",
                fullName, toEmail, otpCode
        );

        sendEmail(toEmail, subject, messageBody);
    }

    /**
     * Dispatches confirmation notification when password has been successfully changed.
     */
    public void sendPasswordChangeSuccessEmail(String toEmail, String fullName) {
        String subject = "Security Alert - Password Changed Successfully";
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String messageBody = String.format(
                "Dear %s,\n\n" +
                "This email confirms that your password for Secure Auth account (%s) was successfully changed.\n\n" +
                "Security Details:\n" +
                "--------------------------------------------------\n" +
                "Status:     Password Changed Successfully\n" +
                "Timestamp:  %s\n" +
                "--------------------------------------------------\n\n" +
                "You can now log in using your newly created password.\n\n" +
                "IMPORTANT SECURITY NOTICE:\n" +
                "If you made this change, you can safely disregard this message.\n" +
                "If you did NOT authorize this change, please contact our security team immediately as your account credentials may be compromised.\n\n" +
                "Best regards,\n" +
                "Secure Auth Security Team",
                fullName, toEmail, timestamp
        );

        sendEmail(toEmail, subject, messageBody);
    }

    /**
     * Dispatches 2FA OTP security alert when login is attempted from an unrecognized device.
     */
    public void sendOtpEmail(String toEmail, String fullName, String otpCode, String deviceId) {
        String subject = "Two-Factor Authentication (OTP) Security Alert";
        String messageBody = String.format(
                "Dear %s,\n\n" +
                "A login attempt was detected from a new or unrecognized device: [%s].\n\n" +
                "Your One-Time Password (OTP) for Two-Factor Authentication is:\n\n" +
                "        ================================\n" +
                "               >>>  %s  <<<\n" +
                "        ================================\n\n" +
                "This OTP is valid for 5 minutes. Do not share this code with anyone.\n" +
                "If you did not initiate this login request, please secure your account immediately.\n\n" +
                "Best regards,\n" +
                "Secure Auth Security Team",
                fullName, deviceId, otpCode
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
